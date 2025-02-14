package org.jetbrains.plugins.scala.findUsages

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi._
import com.intellij.psi.search._
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.Processor
import org.jetbrains.annotations.{NotNull, Nullable}
import org.jetbrains.plugins.scala.extensions.{PsiElementExt, inReadAction}
import org.jetbrains.plugins.scala.lang.psi.ScalaPsiUtil
import org.jetbrains.plugins.scala.lang.psi.api.base.{ScConstructorInvocation, ScReference}
import org.jetbrains.plugins.scala.lang.psi.api.statements.ScTypeAliasDefinition


/**
 * Finds usages of a type alias definition which refers to a class.
 *
 * The usages resolve *directly* to the class, so they are missed by the
 * standard reference search.
 *
 * @see [[org.jetbrains.plugins.scala.lang.resolve.processor.ConstructorResolveProcessor]]
 *
 * {{{
 *   class X()
 *   object A {
 *     type /*search*/alias = X
 *   }
 *   new A./*found*/alias()
 * }}}
 */
class TypeAliasUsagesSearcher extends QueryExecutorBase[PsiReference, ReferencesSearch.SearchParameters](true) {

  override def processQuery(@NotNull parameters: ReferencesSearch.SearchParameters, @NotNull consumer: Processor[_ >: PsiReference]): Unit = {
    val data = inReadAction {
      parameters.getElementToSearch match {
        case target @ ScalaPsiUtil.inNameContext(ta: ScTypeAliasDefinition) =>
          val nm = ta.name
          if (nm != null && !StringUtil.isEmptyOrSpaces(nm))
            Some((target, nm, parameters.getEffectiveSearchScope))
          else None
        case _ => None
      }
    }
    data match {
      case Some((target, name, scope)) =>
        val collector: SearchRequestCollector = parameters.getOptimizer
        val processor = new MyProcessor(target, null)
        collector.searchWord(name, scope, UsageSearchContext.IN_CODE, true, target, processor)
      case _ =>
    }
  }

  private class MyProcessor(myTarget: PsiElement, @Nullable prefix: String) extends RequestResultProcessor(myTarget, prefix) {
    override def processTextOccurrence(element: PsiElement, offsetInElement: Int, consumer: Processor[_ >: PsiReference]): Boolean = inReadAction {
      element.parentOfType(classOf[ScConstructorInvocation], strict = false) match {
        case Some(cons) if PsiTreeUtil.isAncestor(cons.typeElement, element, false) =>
          element match {
            case resRef: ScReference => resRef.bind().flatMap(_.parentElement) match {
              case Some(`myTarget`) => consumer.process(resRef)
              case _ => true
            }
            case _ => true
          }
        case _ => true
      }
    }
  }
}
