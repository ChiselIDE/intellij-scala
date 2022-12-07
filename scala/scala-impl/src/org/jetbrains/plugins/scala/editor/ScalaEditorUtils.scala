package org.jetbrains.plugins.scala.editor

import com.intellij.openapi.editor.Document
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.{PsiElement, PsiFile, PsiWhiteSpace}
import org.jetbrains.annotations.Nullable

object ScalaEditorUtils {

  /**
   * If caret is in the end of the document, file.findElementAt returns null.<br>
   * In this case, this method returns leaf last element in the file if it's non empty.
   */
  @Nullable
  def findElementAtCaret_WithFixedEOF(file: PsiFile, document: Document, caretOffset: Int): PsiElement =
    findElementAtCaret_WithFixedEOF(file, document.getTextLength, caretOffset)

  @Nullable
  def findElementAtCaret_WithFixedEOF(file: PsiFile, documentLength: => Int, caretOffset: Int): PsiElement = {
    val elementAtCaret = file.findElementAt(caretOffset)
    if (elementAtCaret == null && documentLength == caretOffset)
      deepestLastChild(file)
    else
      elementAtCaret
  }

  def deepestLastChild(file: PsiFile): PsiElement = {
    val deepest = PsiTreeUtil.getDeepestLast(file)
    if (deepest eq file) // if file is empty, getDeepestLast returns the file itself
      null
    else
      deepest
  }

  /**
   * If the caret is right after some element but in the beginning of a whitespace this method returns the element
   * Example1: {{{
   *   new MyClass<caret //returns 'MyClass'
   * }}}
   * Example2: {{{
   *   new MyClass   <caret //returns whitespace '   '
   * }}}
   */
  def findElementAtCaret_WithFixedEOFAndWhiteSpace(file: PsiFile, document: Document, caretOffset: Int): PsiElement = {
    val elementAtCaret = file.findElementAt(caretOffset)
    elementAtCaret match {
      case ws: PsiWhiteSpace if caretOffset == ws.getNode.getStartOffset =>
        //in case when caret is right after the error end offset
        PsiTreeUtil.prevLeaf(ws)
      case null if document.getTextLength == caretOffset =>
        deepestLastChild(file)
      case e =>
        e
    }
  }
}
