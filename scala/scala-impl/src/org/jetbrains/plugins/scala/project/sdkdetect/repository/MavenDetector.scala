package org.jetbrains.plugins.scala.project.sdkdetect.repository

import com.intellij.openapi.progress.ProgressIndicator
import org.jetbrains.idea.maven.utils.MavenUtil
import org.jetbrains.plugins.scala.ScalaBundle
import org.jetbrains.plugins.scala.extensions.ObjectExt
import org.jetbrains.plugins.scala.project.template.{PathExt, _}

import java.nio.file.Path
import java.util.stream.{Stream => JStream}

private[repository] object MavenDetector extends ScalaSdkDetectorDependencyManagerBase {

  override def friendlyName: String = ScalaBundle.message("maven.local.repo")

  override protected def buildSdkChoice(descriptor: ScalaSdkDescriptor): SdkChoice = MavenSdkChoice(descriptor)

  override protected def buildJarStream(implicit indicator: ProgressIndicator): JStream[Path] = {
    // TODO Depend on Maven plugin classes optionally
    val mavenHomeDir = MavenUtil.resolveM2Dir().toOption.map(_.toPath)
    val scalaRoot = mavenHomeDir.map(_ / "repository" / "org" / "scala-lang")
    scalaRoot.filter(_.exists).map(collectJarFiles).getOrElse(JStream.empty())
  }
}