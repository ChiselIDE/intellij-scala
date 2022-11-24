package org.jetbrains.sbt.project.template

import com.intellij.ide.util.projectWizard.{ModuleWizardStep, SdkSettingsStep, SettingsStep}
import com.intellij.openapi.diagnostic.ControlFlowException
import com.intellij.openapi.projectRoots.{JavaSdk, SdkTypeId}
import com.intellij.platform.templates.github.ZipUtil
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.plugins.scala.extensions.ObjectExt
import org.jetbrains.sbt.project.template.AbstractArchivedSbtProjectBuilder.{replacePatterns, replacePatterns2}

import java.io.File
import java.net.URL
import java.nio.file.Files
import java.util.regex.Matcher.quoteReplacement
import java.util.regex.Pattern
import java.util.zip.ZipInputStream
import scala.util.Using
import scala.util.control.NonFatal

@ApiStatus.Internal
abstract class AbstractArchivedSbtProjectBuilder extends SbtModuleBuilderBase {

  protected def archiveURL: URL

  override def modifySettingsStep(settingsStep: SettingsStep): ModuleWizardStep =
    new SdkSettingsStep(settingsStep, this, (_: SdkTypeId).is[JavaSdk])

  protected def extractArchive(root: File, url: URL, unwrapSingleTopLevelFolder: Boolean = false): Unit = {
    Using.resource(new ZipInputStream(url.openStream)) { stream =>
      ZipUtil.unzip(null, root.toPath, stream, null, null, unwrapSingleTopLevelFolder)
    }
  }

  override protected def createProjectTemplateIn(root: File): Option[DefaultModuleContentEntryFolders] = {
    if (!root.isDirectory)
      return None

    extractArchive(root, archiveURL)
    processExtractedArchive(root)

    Some(DefaultModuleContentEntryFolders(
      sources = Seq("src/main/scala"),
      testSources = Seq("src/test/scala"),
      resources = Seq("resources"),
      testResources = Seq(),
      excluded = DefaultModuleContentEntryFolders.RootTargets,
    ))
  }

  protected def processExtractedArchive(root: File): Unit = ()

  /**
   * Replaces simple strings in file. Doesn't work with regexps
   * @param replacements a map of simple string substitutions
   * @return new file content or a list of error messages
   */
  protected def replaceInFile(
    root: File,
    relativePath: String,
    replacements: Map[String, String]
  ): Either[Seq[String], String] = {
    val file = new File(root, relativePath)
    val filePath = file.toPath
    if (Files.exists(filePath)) {
      val newContent = try {
        val content = Files.readString(filePath)
        val contentPatched = replacePatterns(content, replacements)
        contentPatched match {
          case Right(result) =>
            Files.writeString(filePath, result)
            Right(result)
          case left@Left(_) =>
            left
        }
      } catch {
        case e: Throwable =>
          Left(Seq(s"Error while processing file $relativePath: ${e.getMessage}"))
      }
      newContent
    }
    else Left(Seq(s"Target file doesn't exist - $relativePath"))
  }

  protected def replaceInFile2(
    root: File,
    relativePath: String,
    replacements: Map[String, String]
  ): Either[Seq[String], String] = {
    modifyFileContent(
      root,
      relativePath,
      replacePatterns2(_, replacements)
    )
  }

  protected def modifyFileContent(
    root: File,
    relativePath: String,
    modifyContent: String => Either[Seq[String], String]
  ): Either[Seq[String], String] = {
    val file = new File(root, relativePath)
    val filePath = file.toPath
    if (Files.exists(filePath)) {
      val newContent = try {
        val content = Files.readString(filePath)
        val contentPatched = modifyContent(content)
        contentPatched match {
          case Right(result) =>
            Files.writeString(filePath, result)
            Right(result)
          case left@Left(_) =>
            left
        }
      } catch {
        case e: Throwable =>
          Left(Seq(s"Error while processing file $relativePath: ${e.getMessage}"))
      }
      newContent
    }
    else Left(Seq(s"Target file doesn't exist - $relativePath"))
  }

}

object AbstractArchivedSbtProjectBuilder {

  implicit class SbtPatternExt(val str: String) extends AnyVal {
    private def escaped(s: String) =
      s.replaceAll("\\s+", quoteReplacement("\\s+"))

    def keyInit: String =
      s"""(^.+${escaped(str)}\\s*:=\\s*)([^\\s][^,]+)(,.+$$)"""

    def keyInitQuoted: String =
      s"""(^.+${escaped(str)}\\s*:=\\s*")([^",]+)(",.+$$)"""

    def tagBody: String =
      s"""(^.+<$str>)([^<>]+)(</$str>.+$$)"""

    def emptyTagAttr: String = {
      val Array(tag, attr) = str.split('/')
      s"""(^.+<$tag.+$attr=")([^"]+)(".*/>.+$$)"""
    }
  }

  def replacePatterns(content: String, replacements: Map[String, String]): Either[Seq[String], String] = {
    val (result, errors) = replacements.foldLeft(content -> Seq.empty[String]) ({ case ((text, errors), (from, to)) =>
      try {
        val pattern = Pattern.compile(from, Pattern.DOTALL)
        val matcher = pattern.matcher(text)
        if (matcher.matches())
          matcher.replaceFirst(s"$$1$to$$3") -> errors
        else
          text -> (errors :+ s"Key '$from' not found")
      } catch {
        case c: ControlFlowException =>
          throw c
        case NonFatal(e: Exception) =>
          text -> (errors :+ s"Exception during patching '$from': ${e.getMessage}'")
      }
    })

    if (errors.isEmpty) Right(result)
    else Left(errors)
  }

  def replacePatterns2(content: String, replacements: Map[String, String]): Either[Seq[String], String] = {
    val (result, errors) = replacements.foldLeft(content -> Seq.empty[String]) ({ case ((text, errors), (from, to)) =>
      try {
        if (!text.contains(from))
          text -> (errors :+ s"Key '$from' not found")
        else
          text.replace(from, to) -> errors
      } catch {
        case c: ControlFlowException =>
          throw c
        case NonFatal(e: Exception) =>
          text -> (errors :+ s"Exception during patching '$from': ${e.getMessage}'")
      }
    })

    if (errors.isEmpty) Right(result)
    else Left(errors)
  }

}