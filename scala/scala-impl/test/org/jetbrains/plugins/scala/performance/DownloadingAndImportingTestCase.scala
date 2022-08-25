package org.jetbrains.plugins.scala.performance

import com.intellij.lang.javascript.boilerplate.GithubDownloadUtil
import com.intellij.platform.templates.github.ZipUtil
import org.jetbrains.plugins.scala.util.TestUtils
import org.junit.Assert

import java.io.File

abstract class DownloadingAndImportingTestCase extends ImportingProjectTestCase {

  implicit class IntExt(private val i: Int) {
    def seconds: Int = i * 1000
  }

  override def rootProjectsDirPath: String = DownloadingAndImportingTestCase.rootProjectsDirPath

  override def projectName: String = githubRepositoryWithRevision.repositoryName

  protected def githubRepositoryWithRevision: GithubRepositoryWithRevision

  def outputZipFileName: String = {
    val GithubRepositoryWithRevision(userName, repoName, revision) = githubRepositoryWithRevision
    s"$rootProjectsDirPath/zipFiles/$repoName-$userName-$revision"
  }

  override def doBeforeImport(): Unit = downloadAndExtractProject()

  private def downloadAndExtractProject(): Unit = {
    val outputZipFile = new File(outputZipFileName)
    val projectDir = new File(projectDirPath)

    reporter.notify(s"Project output zip file: $outputZipFile")
    reporter.notify(s"Project directory: $projectDir")

    if (outputZipFile.exists() || projectDir.exists()) {
      reporter.notify("Project files already exist, skipping download")
    } else {
      //don't download if zip file is already there
      reporter.notify(s"Starting download")
      GithubDownloadUtil.downloadAtomically(
        reporter.progressIndicator,
        githubRepositoryWithRevision.revisionDownloadUrl,
        outputZipFile,
        githubRepositoryWithRevision.userName,
        githubRepositoryWithRevision.repositoryName
      )
    }

    if (projectDir.exists()) {
      //don't unpack if the project is already unpacked
      reporter.notify("Project files already extracted")
    } else {
      reporter.notify("Finished download, extracting")
      ZipUtil.unzip(null, projectDir, outputZipFile, null, null, true)
    }

    Assert.assertTrue("Project dir does not exist. Download or unpack failed!", projectDir.exists())
    reporter.notify("Finished extracting, starting sbt setup")
  }
}

object DownloadingAndImportingTestCase {
  def rootProjectsDirPath: String = s"${TestUtils.getTestDataPath}/projects"
}

case class GithubRepositoryWithRevision(
  userName: String,
  repositoryName: String,
  revision: String,
) {
  def repositoryUrl: String = s"https://github.com/$userName/$repositoryName"

  def revisionDownloadUrl: String = s"$repositoryUrl/archive/$revision.zip"
}

object GithubRepositoryWithRevision {
  val ScalaCommunityGithubRepo: GithubRepositoryWithRevision = GithubRepositoryWithRevision(
    "JetBrains",
    "intellij-scala",
    "a9ac902e8930c520b390095d9e9346d9ae546212"
  )
}