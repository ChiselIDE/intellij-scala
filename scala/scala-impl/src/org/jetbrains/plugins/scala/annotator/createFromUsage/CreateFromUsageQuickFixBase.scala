package org.jetbrains.plugins.scala.annotator.createFromUsage

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.{PsiDocumentManager, PsiFile}
import org.jetbrains.plugins.scala.extensions.ObjectExt
import org.jetbrains.plugins.scala.lang.psi.api.ScalaFile
import org.jetbrains.plugins.scala.lang.psi.api.base.ScReference
import org.jetbrains.plugins.scala.lang.psi.impl.source.ScalaCodeFragment
import org.jetbrains.plugins.scala.statistics.ScalaActionUsagesCollector

abstract class CreateFromUsageQuickFixBase(ref: ScReference)
  extends IntentionAction {

  implicit val project: Project = ref.getProject

  override def isAvailable(project: Project, editor: Editor, file: PsiFile): Boolean =
    if (file.is[ScalaCodeFragment]) false
    else if (!file.is[ScalaFile]) false
    else if (!ref.isValid) false
    else true

  override def startInWriteAction() = false

  override def invoke(project: Project, editor: Editor, file: PsiFile): Unit = {
    PsiDocumentManager.getInstance(project).commitAllDocuments()
    if (!ref.isValid) return

    ScalaActionUsagesCollector.logCreateFromUsage(project)
    invokeInner(project, editor, file)
  }

  protected def invokeInner(project: Project, editor: Editor, file: PsiFile): Unit
}
