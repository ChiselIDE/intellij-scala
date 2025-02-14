package org.jetbrains.plugins.scala.refactoring.move.member

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.psi.PsiDocumentManager
import org.jetbrains.plugins.scala.base.ScalaLightCodeInsightFixtureTestCase
import org.jetbrains.plugins.scala.extensions.{PsiMemberExt, StringExt}
import org.jetbrains.plugins.scala.lang.psi.ElementScope
import org.jetbrains.plugins.scala.lang.psi.impl.ScalaFileImpl
import org.jetbrains.plugins.scala.lang.refactoring.move.members.ScalaMoveMembersDialog
import org.junit.Assert

import scala.annotation.nowarn

abstract class BaseScalaMoveMemberTest extends ScalaLightCodeInsightFixtureTestCase {

  def doTest(fromObject: String, toObject: String, memberName: String, fileText: String, expectedText: String): Unit = {
    configureFromFileText("dummy.scala", fileText.withNormalizedSeparator.trim)
    performAction(fromObject, toObject, memberName)
    Assert.assertEquals(expectedText.withNormalizedSeparator.trim, getFile.getText)
  }

  private def performAction(fromObject: String, toObject: String, memberName: String): Unit = {
    val projectScope = ElementScope(getProject)
    val source = projectScope.getCachedObject(fromObject).orNull
    val target = projectScope.getCachedObject(toObject).orNull

    Assert.assertTrue(s"file $fromObject not found", source != null)
    Assert.assertTrue(s"file $target not found", target != null)

    val member = source.membersWithSynthetic.find(_.names.contains(memberName)).get

    val processor = ScalaMoveMembersDialog.createProcessor(target, member)
    ScalaFileImpl.performMoveRefactoring(processor.run())

    FileDocumentManager.getInstance.saveAllDocuments()
    PsiDocumentManager.getInstance(getProject).commitAllDocuments()
  }
}
