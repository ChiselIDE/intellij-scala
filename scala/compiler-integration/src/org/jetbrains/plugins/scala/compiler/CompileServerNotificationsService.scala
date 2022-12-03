package org.jetbrains.plugins.scala.compiler

import com.intellij.notification.{Notification, NotificationAction, NotificationType, Notifications}
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.JavaSdkVersion
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.CompositeModificationTracker
import org.jetbrains.plugins.scala.extensions.{executeOnPooledThread, invokeAndWait}
import org.jetbrains.plugins.scala.macroAnnotations.Cached
import org.jetbrains.plugins.scala.project.ProjectExt
import org.jetbrains.plugins.scala.settings.{ScalaCompileServerSettings, ScalaHighlightingMode}

import scala.annotation.nowarn
import scala.concurrent.duration.DurationLong

@Service
final class CompileServerNotificationsService(project: Project) {

  private val title = CompilerIntegrationBundle.message("scala.compile.server.title")
  
  private val modificationTracker = new CompositeModificationTracker(ProjectRootManager.getInstance(project))
  
  def resetNotifications(): Unit =
    modificationTracker.incModificationCount()

  /**
   * SCL-18187
   * SCL-17817
   */
  @nowarn("msg=pure expression")
  @Cached(modificationTracker, null)
  def warnIfCompileServerJdkMayLeadToCompilationProblems(): Unit = if (project.hasScala) {
    def serverJdkIsOk(serverJdkVersion: JavaSdkVersion, recommendedJdkVersion: JavaSdkVersion): Boolean =
      if (ScalaHighlightingMode.isShowErrorsFromCompilerEnabled(project))
        serverJdkVersion == recommendedJdkVersion
      else
        serverJdkVersion isAtLeast recommendedJdkVersion
    for {
      (serverSdk, serverJdkVersion) <- CompileServerJdkManager.compileServerJdk(project)
      (recommendedSdk, recommendedJdkVersion) <- CompileServerJdkManager.recommendedJdk(project)
      if !serverJdkIsOk(serverJdkVersion, recommendedJdkVersion)
    } showWarning(serverSdk.getName, recommendedSdk.getName)
  }

  private def showWarning(serverSdk: String, recommendedSdk: String): Unit = {
    val text = s"""We recommend using JDK '$recommendedSdk' to prevent compilation issues (current JDK is '$serverSdk')""".stripMargin
    val notification = new Notification(title, title, text, NotificationType.WARNING)
    notification.addAction(new FixSdkAction(recommendedSdk))
    notification.addAction(new OpenScalaCompileServerSettingsAction(project, filter = "JDK"))
    Notifications.Bus.notify(notification)
  }
  
  private class FixSdkAction(fixedSdk: String) extends NotificationAction(CompilerIntegrationBundle.message("wrong.jdk.action.use.jdk", fixedSdk)) {

    override def actionPerformed(e: AnActionEvent, notification: Notification): Unit = {
      notification.expire()
      val settings = ScalaCompileServerSettings.getInstance
      if (CompileServerLauncher.defaultSdk(project).getName == fixedSdk) {
        settings.USE_DEFAULT_SDK = true
      } else {
        settings.USE_DEFAULT_SDK = false
        settings.COMPILE_SERVER_SDK = fixedSdk
      }
      ApplicationManager.getApplication.saveSettings()
      executeOnPooledThread {
        CompileServerLauncher.stop(timeoutMs = 3.seconds.toMillis)
        CompileServerLauncher.ensureServerRunning(project)
      }
    }
  }
}

object CompileServerNotificationsService {
  
  def get(project: Project): CompileServerNotificationsService =
    project.getService(classOf[CompileServerNotificationsService])
}
