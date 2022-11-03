package org.jetbrains.plugins.scala.lang.formatting.settings.migration

import com.intellij.testFramework.LightIdeaTestCase
import org.jetbrains.plugins.scala.lang.formatting.settings.migration.CodeStyleSettingsMigrationServiceBase.Migrations
import org.jetbrains.plugins.scala.util.TestUtils
import org.junit.Assert._

class CodeStyleSettingsMigrationServiceBaseTest extends LightIdeaTestCase {

  override def setUp(): Unit = {
    TestUtils.optimizeSearchingForIndexableFiles()
    super.setUp()
  }

  def testValidateDefinedMigrations(): Unit = {
    val versions = Migrations.all.map(_.version)
    assertTrue("migrations versions should be unique", versions.distinct.size == versions.size)
    assertTrue("migrations versions should increment", versions.sorted == versions)
  }

}