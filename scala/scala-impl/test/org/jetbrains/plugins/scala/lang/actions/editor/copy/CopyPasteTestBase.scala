package org.jetbrains.plugins.scala.lang.actions.editor.copy

import com.intellij.lang.ASTNode
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.testFramework.EditorTestUtil
import org.jetbrains.plugins.scala.base.{ScalaLightCodeInsightFixtureTestAdapter, SharedTestProjectToken}
import org.jetbrains.plugins.scala.lang.formatting.settings.ScalaCodeStyleSettings
import org.jetbrains.plugins.scala.util.TypeAnnotationSettings
import org.junit.Assert.assertTrue

import scala.collection.mutable.ArrayBuffer

abstract class CopyPasteTestBase extends ScalaLightCodeInsightFixtureTestAdapter {
  protected val Start = EditorTestUtil.SELECTION_START_TAG
  protected val End = EditorTestUtil.SELECTION_END_TAG
  protected val Caret = EditorTestUtil.CARET_TAG

  protected val tab = "\t"
  protected val empty = ""

  def fromLangExtension: String = "scala"

  //copy/paste action might involve collection of some information which requires type inference & resolve
  //(for example see org.jetbrains.plugins.scala.lang.refactoring.Associations.collectAssociations)
  //files from previous tests might affect this process, so we want to be clean
  override protected def sharedProjectToken: SharedTestProjectToken = SharedTestProjectToken.DoNotShare

  private var oldSettings: ScalaCodeStyleSettings = _
  private var oldBlankLineSetting: Int = _

  private val myASTHardRefs: ArrayBuffer[ASTNode] = ArrayBuffer.empty

  override protected def setUp(): Unit = {
    super.setUp()

    val project = getProject
    oldSettings = ScalaCodeStyleSettings.getInstance(project)
    oldBlankLineSetting = oldSettings.BLANK_LINES_AROUND_METHOD_IN_INNER_SCOPES
    oldSettings.BLANK_LINES_AROUND_METHOD_IN_INNER_SCOPES = 0
    TypeAnnotationSettings.set(project, TypeAnnotationSettings.alwaysAddType(oldSettings))
  }

  override def tearDown(): Unit = {
    myASTHardRefs.clear()
    val project = getProject
    ScalaCodeStyleSettings.getInstance(project).BLANK_LINES_AROUND_METHOD_IN_INNER_SCOPES = oldBlankLineSetting
    TypeAnnotationSettings.set(project, oldSettings)
    super.tearDown()
  }

  protected final def doTest(from: String, to: String, after: String): Unit = {
    doTest(from, to, after, s"from.$fromLangExtension", "to.scala")
  }

  //NOTE: in IntelliJ Platform tests for copy/paste
  //they use IdeActions.ACTION_EDITOR_COPY/ACTION_EDITOR_PASTE
  //instead of IdeActions.ACTION_COPY & IdeActions.ACTION_PASTE
  //I am not sure what is the difference though
  protected def doTest(from: String, to: String, after: String, fromFileName: String, toFileName: String): Unit = {
    def normalize(s: String): String = s.replace("\r", "")

    assertTrue("Content of target file doesn't contain caret marker", to.contains(Caret))

    val fileFrom = myFixture.configureByText(fromFileName, normalize(from))
    myASTHardRefs += fileFrom.getNode
    myFixture.performEditorAction(IdeActions.ACTION_COPY)

    val fileTo = myFixture.configureByText(toFileName, normalize(to))
    myASTHardRefs += fileTo.getNode
    myFixture.performEditorAction(IdeActions.ACTION_PASTE)

    myFixture.checkResult(normalize(after), true)
  }

  /**
   * The test tests that with any existing selection in the editor,
   * when we paste some code the selected code should be removed and replaced with the pasted code.
   * So it shouldn't matter which selection is there in the editor, the result should be the same
   */
  protected def doTestWithAllSelections(from: String, to: String, after: String): Unit = {
    val textsWithSelections = Seq(
      s"""$Caret""",
      s"""$Caret$Start$End""",
      s"""$Caret$Start  $End""",
      s"""$Caret$Start$tab$End""",
      s"""$Caret$Start
         |$End""".stripMargin,
      s"""$Caret$Start$tab$empty
         |  $End""".stripMargin,
      s"""$Caret${Start}print("Existing code 1")$End""",
      s"""$Caret$Start
         |  print("Existing code 2")$tab$empty
         | $End""".stripMargin,
    )

    val uniqueToken = System.currentTimeMillis
    for (case (textWithSelection, index) <- textsWithSelections.zipWithIndex) {
      val toModified = to.replaceAll(Caret, textWithSelection)
      val fromFileName = s"from-$uniqueToken-$index.$fromLangExtension"
      val toFileName = s"to-$uniqueToken-$index.scala"
      doTest(from, toModified, after, fromFileName, toFileName)
    }
  }

  protected def doTestToEmptyFile(fromText: String, expectedText: String): Unit = {
    doTest(fromText, Caret, expectedText)
  }
}