package org.jetbrains.plugins.scala.annotator.element

import com.intellij.psi.PsiElement
import org.jetbrains.plugins.scala.ScalaBundle
import org.jetbrains.plugins.scala.annotator.ScalaAnnotationHolder
import org.jetbrains.plugins.scala.lang.psi.ScalaPsiUtil.superValsSignatures
import org.jetbrains.plugins.scala.extensions.PsiNamedElementExt
import org.jetbrains.plugins.scala.lang.psi.api.statements.params.ScClassParameter
import org.jetbrains.plugins.scala.lang.psi.api.statements.{ScFunction, ScValue, ScValueOrVariable, ScVariable}
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.ScTemplateDefinition
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.{ScModifierListOwner, ScTypedDefinition}

object ScOverriddenVarAnnotator extends ElementAnnotator[ScTypedDefinition] {
  override def annotate(elem: ScTypedDefinition, typeAware: Boolean)
                       (implicit holder: ScalaAnnotationHolder): Unit = {
    val errorMessage = elem.nameContext match {
      case o: ScModifierListOwner if !o.getModifierList.isOverride =>
        None
      case IsAbstract(_) =>
        None
      case v if isVar(v) =>
        val supers = superValsSignatures(elem, withSelfType = true).map { s => s.namedElement.nameContext }
        if (supers.exists(!isAbstract(_)))
          Some(ScalaBundle.message("var.cannot.be.overridden"))
        else
          None
      case f: ScFunction if f.name.endsWith("_=") =>
        val elemName = f.name.dropRight(2)
        val supers = f.superSignatures.map { _.namedElement.nameContext }
        if (supers.exists(isVar) && !hasGetter(elemName, f.containingClass))
          Some(ScalaBundle.message("missing.getter.implementation", elemName))
        else
          None
      case f: ScFunction if f.isParameterless =>
        val supers = f.superSignatures.map { _.namedElement.nameContext }
        checkForNonAbstractVarSuper(supers, elem.name, f.containingClass)
      case v: ScValue =>
        val supers = superValsSignatures(elem, withSelfType = true).map { _.namedElement.nameContext }
        checkForNonAbstractVarSuper(supers, elem.name, v.containingClass)
      case _: ScClassParameter =>
        val supers = superValsSignatures(elem, withSelfType = true).map { _.namedElement.nameContext }
        if (supers.exists(isVar))
          Some(ScalaBundle.message("var.cannot.be.overridden"))
        else
          None
      case _ =>
        None
    }

    errorMessage.foreach(holder.createErrorAnnotation(elem.getIdentifyingElement, _))
  }

  private def checkForNonAbstractVarSuper(supers: Seq[PsiElement], elemName: String, containingClass: ScTemplateDefinition): Option[String] =
    supers.collectFirst {
      case p: ScClassParameter if p.isVar =>
        ScalaBundle.message("var.cannot.be.overridden")
      case v: ScVariable if v.isAbstract && !hasSetter(elemName, containingClass) =>
        ScalaBundle.message("missing.setter.implementation", elemName)
      case v: ScVariable if !v.isAbstract =>
        ScalaBundle.message("var.cannot.be.overridden")
    }

  private def isVar(elem: PsiElement): Boolean =
    elem match {
      case p: ScClassParameter => p.isVar
      case _: ScVariable       => true
      case _                   => false
    }

  private def isAbstract(elem: PsiElement): Boolean =
    elem match {
      case v: ScValueOrVariable => v.isAbstract
      case d: ScTypedDefinition => d.isAbstractMember
      case _                    => false
    }

  private object IsAbstract {
    def unapply(elem: PsiElement): Option[PsiElement] = Option(elem).find(isAbstract)
  }

  private def hasGetter(elemName: String, containingClass: ScTemplateDefinition): Boolean =
    containingClass.allVals.exists(v => !v.isAbstract && v.name == elemName) ||
      containingClass.allFunctionsByName(elemName).exists {
        case _: ScFunction => true
        case _ => false
      }

  private def hasSetter(elemName: String, containingClass: ScTemplateDefinition): Boolean =
    containingClass.allFunctionsByName(elemName + "_=").exists {
      case _: ScFunction => true
      case _ => false
    }
}
