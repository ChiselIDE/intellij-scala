package org.jetbrains.plugins.scala
package codeInsight
package daemon

import com.intellij.codeInsight.daemon.impl.HighlightVisitor
import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.project.Project
import com.intellij.psi._
import org.jetbrains.plugins.scala.annotator.ScalaAnnotator
import org.jetbrains.plugins.scala.annotator.hints.AnnotatorHints
import org.jetbrains.plugins.scala.annotator.usageTracker.ScalaRefCountHolder
import org.jetbrains.plugins.scala.annotator.usageTracker.UsageTracker._
import org.jetbrains.plugins.scala.caches.CachesUtil.fileModCount
import org.jetbrains.plugins.scala.extensions.{ObjectExt, PsiElementExt, PsiFileExt}
import org.jetbrains.plugins.scala.lang.psi.ScalaPsiUtil
import org.jetbrains.plugins.scala.lang.psi.api.ImplicitArgumentsOwner
import org.jetbrains.plugins.scala.lang.psi.api.base.ScReference
import org.jetbrains.plugins.scala.lang.psi.api.expr._

final class ScalaRefCountVisitor(project: Project) extends HighlightVisitor {

  private var myRefCountHolder: ScalaRefCountHolder = _

  override def suitableForFile(file: PsiFile): Boolean =
    ScalaAnnotator.isSuitableForFile(file)

  override def visit(element: PsiElement): Unit =
    registerElementsAndImportsUsed(element)

  override def analyze(file: PsiFile,
                       updateWholeFile: Boolean,
                       holder: HighlightInfoHolder,
                       analyze: Runnable): Boolean = {
    val scalaFile = file.findScalaLikeFile.orNull
    if (scalaFile == null)
      return true

    if (InjectedLanguageManager.getInstance(project).isInjectedFragment(scalaFile))
      return true

    clearDirtyAnnotatorHintsIn(scalaFile)
    var success = true
    try {
      if (updateWholeFile) {
        myRefCountHolder = ScalaRefCountHolder.get(scalaFile)
        success = myRefCountHolder.analyze(analyze, scalaFile)
      } else {
        myRefCountHolder = null
        analyze.run()
      }
    } finally {
      myRefCountHolder = null
    }
    success
  }

  private def registerElementsAndImportsUsed(element: PsiElement): Unit = {
    element match {
      case ref: ScReference =>
        val resolve = ref.multiResolveScala(false)
        registerUsedElementsAndImports(ref, resolve, checkWrite = true)
      case selfInv: ScSelfInvocation =>
        val resolve = selfInv.multiResolve
        registerUsedElementsAndImports(selfInv, resolve, checkWrite = false)
      case f: ScFor =>
        registerUsedImports(f, ScalaPsiUtil.getExprImports(f))
      case call: ScMethodCall =>
        registerUsedImports(call, call.getImportsUsed)

      case ret: ScReturn =>
        val importUsed = ret.expr
          .toSet[ScExpression]
          .flatMap(_.getTypeAfterImplicitConversion().importsUsed)
        registerUsedImports(element, importUsed)
      case _ =>
    }

    element.asOptionOf[ScExpression]
      .foreach { expr =>
        val fromUnderscore = ScUnderScoreSectionUtil.isUnderscoreFunction(expr)
        val importUsed = expr.getTypeAfterImplicitConversion(fromUnderscore = fromUnderscore).importsUsed

        registerUsedImports(element, importUsed)
      }

    element.asOptionOf[ImplicitArgumentsOwner]
      .foreach { owner =>
        owner.findImplicitArguments.foreach { params =>
          registerUsedElementsAndImports(element, params, checkWrite = false)
        }
      }
  }

  // Annotator hints, SCL-15593
  private def clearDirtyAnnotatorHintsIn(file: PsiFile): Unit = {
    val dirtyScope = ScalaRefCountHolder.findDirtyScope(file).flatten

    file.elements.foreach { element =>
      if (AnnotatorHints.in(element).exists(_.modificationCount < fileModCount(file)) &&
        dirtyScope.forall(_.contains(element.getTextRange))) {

        AnnotatorHints.clearIn(element)
      }
    }
  }

  override def clone = new ScalaRefCountVisitor(project)
}