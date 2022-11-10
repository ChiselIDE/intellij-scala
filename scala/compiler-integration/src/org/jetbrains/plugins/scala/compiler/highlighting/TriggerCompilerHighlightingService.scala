package org.jetbrains.plugins.scala.compiler.highlighting

import com.intellij.codeInsight.daemon.impl.UpdateHighlightersUtil
import com.intellij.codeInsight.daemon.impl.analysis.{FileHighlightingSetting, FileHighlightingSettingListener}
import com.intellij.ide.PowerSaveMode
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.{Document, EditorFactory}
import com.intellij.openapi.fileEditor.{FileDocumentManager, FileEditor}
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.{JavaProjectRootsUtil, ProjectRootManager, TestSourcesFilter}
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.problems.WolfTheProblemSolver
import com.intellij.psi._
import org.jetbrains.jps.incremental.scala.remote.SourceScope
import org.jetbrains.plugins.scala.extensions._
import org.jetbrains.plugins.scala.lang.psi.api.ScalaFile
import org.jetbrains.plugins.scala.settings.{ScalaCompileServerSettings, ScalaHighlightingMode}

import scala.collection.concurrent.TrieMap
import scala.jdk.CollectionConverters._
import scala.util.control.NonFatal

@Service
private[scala] final class TriggerCompilerHighlightingService(project: Project) extends Disposable {

  import TriggerCompilerHighlightingService._

  private val documentCompilerAvailable: TrieMap[VirtualFile, java.lang.Boolean] = TrieMap.empty

  project.getMessageBus.connect(this).subscribe[FileHighlightingSettingListener](
    FileHighlightingSettingListener.SETTING_CHANGE,
    (root: PsiElement, _: FileHighlightingSetting) => {
      val psiFile = root.getContainingFile
      if (psiFile ne null) {
        val virtualFile = psiFile.getVirtualFile
        if (virtualFile ne null) {
          val document = FileDocumentManager.getInstance().getDocument(virtualFile)
          EditorFactory.getInstance().getEditors(document).foreach { editor =>
            UpdateHighlightersUtil.setHighlightersToEditor(
              project, document,
              0, document.getTextLength, Seq.empty.asJava,
              editor.getColorsScheme, ExternalHighlighters.ScalaCompilerPassId)
          }
          executeOnPooledThread {
            WolfTheProblemSolver.getInstance(project).clearProblemsFromExternalSource(virtualFile, ExternalHighlighters)
          }

          if (isHighlightingEnabled && isHighlightingEnabledFor(psiFile, virtualFile)) {
            val debugReason = s"FileHighlightingSetting changed for ${virtualFile.getCanonicalPath}"
            triggerIncrementalCompilation(debugReason, virtualFile, document, psiFile)
          }
        }
      }
    }
  )

  private[highlighting] def triggerOnFileChange(psiFile: PsiFile, virtualFile: VirtualFile): Unit = {
    if (isHighlightingEnabled && isHighlightingEnabledFor(psiFile, virtualFile) && !hasErrors(psiFile)) {
      val debugReason = s"file content changed: ${psiFile.name}"
      val document = FileDocumentManager.getInstance().getDocument(virtualFile)
      if (document ne null) {
        if (psiFile.isScalaWorksheet) {
          triggerWorksheetCompilation(virtualFile, psiFile.asInstanceOf[ScalaFile], document, debugReason)
        } else {
          if (documentCompilerAvailable.contains(virtualFile)) {
            triggerDocumentCompilation(virtualFile, document, debugReason)
          } else {
            triggerIncrementalCompilation(debugReason, virtualFile, document, psiFile)
          }
        }
      }
    }
  }

  private[highlighting] def triggerOnSelectionChange(editor: FileEditor): Unit = {
    if (isHighlightingEnabled && ScalaHighlightingMode.isShowErrorsFromCompilerEnabled(project)) {
      val virtualFile = editor.getFile
      if (virtualFile ne null) {
        val psiFile = inReadAction(PsiManager.getInstance(project).findFile(virtualFile))
        if ((psiFile ne null) && isHighlightingEnabledFor(psiFile, virtualFile) && !hasErrors(psiFile)) {
          val document = inReadAction(FileDocumentManager.getInstance().getDocument(virtualFile))
          if (document ne null) {
            val debugReason = s"selected editor changed: ${virtualFile.getName}"
            if (psiFile.isScalaWorksheet)
              triggerWorksheetCompilation(virtualFile, psiFile.asInstanceOf[ScalaFile], document, debugReason)
            else
              triggerIncrementalCompilation(debugReason, virtualFile, document, psiFile)
          }
        }
      }
    }
  }

  override def dispose(): Unit = {
    documentCompilerAvailable.clear()
  }

  private def isHighlightingEnabled: Boolean =
    !PowerSaveMode.isEnabled && ScalaCompileServerSettings.getInstance.COMPILE_SERVER_ENABLED

  private def isHighlightingEnabledFor(psiFile: PsiFile, virtualFile: VirtualFile): Boolean = {
    ScalaHighlightingMode.isShowErrorsFromCompilerEnabled(psiFile) &&
      virtualFile.isInLocalFileSystem &&
      (psiFile match {
        case _ if psiFile.isScalaWorksheet => true
        case _: ScalaFile | _: PsiJavaFile if !JavaProjectRootsUtil.isOutsideJavaSourceRoot(psiFile) => true
        case _ => false
      }) &&
      ScalaHighlightingMode.shouldHighlightBasedOnFileLevel(psiFile, project)
  }

  private def triggerIncrementalCompilation(debugReason: String, virtualFile: VirtualFile, document: Document, psiFile: PsiFile): Unit = {
    val module = ProjectRootManager.getInstance(project).getFileIndex.getModuleForFile(virtualFile)
    if (module ne null) {
      val sourceScope = calculateSourceScope(virtualFile)
      CompilerHighlightingService.get(project)
        .triggerIncrementalCompilation(virtualFile, module, sourceScope, document, psiFile, debugReason)
    }
  }

  def beforeIncrementalCompilation(): Unit = invokeAndWait {
    val fileDocumentManager = FileDocumentManager.getInstance()
    val psiDocumentManager = PsiDocumentManager.getInstance(project)
    val unsaved = fileDocumentManager.getUnsavedDocuments
    unsaved.filter(psiDocumentManager.getPsiFile(_) ne null).foreach { document =>
      try fileDocumentManager.saveDocumentAsIs(document)
      catch {
        case NonFatal(_) =>
      }
    }
  }

  def enableDocumentCompiler(virtualFile: VirtualFile): Unit = {
    DocumentCompiler.get(project).clearOutputDirectories()
    documentCompilerAvailable.put(virtualFile, java.lang.Boolean.TRUE)
  }

  def disableDocumentCompiler(virtualFile: VirtualFile): Unit = {
    documentCompilerAvailable.remove(virtualFile, java.lang.Boolean.TRUE)
  }

  private def triggerDocumentCompilation(
    virtualFile: VirtualFile,
    document: Document,
    debugReason: String
  ): Unit = {
    val sourceScope = calculateSourceScope(virtualFile)
    CompilerHighlightingService.get(project).triggerDocumentCompilation(virtualFile, document, sourceScope, debugReason)
  }

  private def triggerWorksheetCompilation(
    virtualFile: VirtualFile,
    psiFile: ScalaFile,
    document: Document,
    debugReason: String
  ): Unit =
    CompilerHighlightingService.get(project).triggerWorksheetCompilation(
      virtualFile,
      psiFile,
      document,
      debugReason
    )

  private def calculateSourceScope(file: VirtualFile): SourceScope =
    if (TestSourcesFilter.isTestSources(file, project)) SourceScope.Test
    else SourceScope.Production
}

private[scala] object TriggerCompilerHighlightingService {

  def get(project: Project): TriggerCompilerHighlightingService =
    project.getService(classOf[TriggerCompilerHighlightingService])

  private def hasErrors(psiFile: PsiFile): Boolean =
    psiFile.elements.findByType[PsiErrorElement].isDefined
}
