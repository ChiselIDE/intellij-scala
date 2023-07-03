package org.jetbrains.plugins.scala.lang.psi.impl.statements

import com.intellij.lang.ASTNode
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.{ProcessCanceledException, ProgressManager}
import com.intellij.openapi.project.DumbService
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.{PsiClass, PsiElement, ResolveState}
import org.jetbrains.plugins.scala.caches.{BlockModificationTracker, cachedInUserData}
import org.jetbrains.plugins.scala.extensions.{ObjectExt, PsiElementExt}
import org.jetbrains.plugins.scala.icons.Icons
import org.jetbrains.plugins.scala.lang.lexer.{ScalaTokenType, ScalaTokenTypes}
import org.jetbrains.plugins.scala.lang.psi.ScalaPsiUtil
import org.jetbrains.plugins.scala.lang.psi.api.base.ScModifierList
import org.jetbrains.plugins.scala.lang.psi.api.statements.params.ScTypeParam
import org.jetbrains.plugins.scala.lang.psi.api.statements.{ScEnumCase, ScEnumCases, ScPatternDefinition}
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.{ScClass, ScEnum, ScTypeDefinition}
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.{ScNamedElement, ScTypeParametersOwner}
import org.jetbrains.plugins.scala.lang.psi.impl.ScalaPsiElementFactory
import org.jetbrains.plugins.scala.lang.psi.impl.statements.ScEnumCaseImpl.Log
import org.jetbrains.plugins.scala.lang.psi.impl.toplevel.typedef.ScTypeDefinitionImpl
import org.jetbrains.plugins.scala.lang.psi.stubs.ScTemplateDefinitionStub
import org.jetbrains.plugins.scala.lang.psi.stubs.elements.ScTemplateDefinitionElementType
import org.jetbrains.plugins.scala.lang.psi.types.api.designator.ScDesignatorType
import org.jetbrains.plugins.scala.lang.psi.types.api.{Nothing, ParameterizedType, TypeParameterType}
import org.jetbrains.plugins.scala.lang.psi.types.{ScType, ScalaType}
import org.jetbrains.plugins.scala.lang.resolve.processor.BaseProcessor

import javax.swing.Icon
import scala.util.control.NonFatal

final class ScEnumCaseImpl(
  stub:      ScTemplateDefinitionStub[ScEnumCase],
  nodeType:  ScTemplateDefinitionElementType[ScEnumCase],
  node:      ASTNode,
  debugName: String
) extends ScTypeDefinitionImpl(stub, nodeType, node, debugName)
    with ScTypeParametersOwner
    with ScEnumCase {

  import ScalaTokenTypes.kCASE

  override def processDeclarations(
    processor:  PsiScopeProcessor,
    state:      ResolveState,
    lastParent: PsiElement,
    place:      PsiElement
  ): Boolean =
    processDeclarationsImpl(processor, state, lastParent, place)

  override def processDeclarationsForTemplateBody(
    processor:  PsiScopeProcessor,
    state:      ResolveState,
    lastParent: PsiElement,
    place:      PsiElement
  ): Boolean = {
    if (DumbService.getInstance(getProject).isDumb) return true

    if (!super.processDeclarationsForTemplateBody(processor, state, lastParent, place)) return false

    constructor match {
      case Some(constr) if place != null && PsiTreeUtil.isContextAncestor(constr, place, false) =>
      //ignore, should be processed in ScParameters
      case _ =>
        for (p <- parameters) {
          ProgressManager.checkCanceled()
          if (processor.is[BaseProcessor]) {
            if (!processor.execute(p, state)) return false
          }
        }
    }

    super[ScTypeParametersOwner].processDeclarations(processor, state, lastParent, place)
  }

  override def getModifierList: ScModifierList = getParentByStub match {
    case cases: ScEnumCases => cases.getModifierList
    case _                  => ScalaPsiElementFactory.createEmptyModifierList(this)
  }

  override def enumParent: ScEnum =
    this.getStubOrPsiParentOfType(classOf[ScEnum])

  def physicalTypeParameters: Seq[ScTypeParam] = super.typeParameters

  override def typeParameters: Seq[ScTypeParam] = cachedInUserData("typeParameters", this, BlockModificationTracker(this)) {
    if (super.typeParameters.isEmpty) {
      try {
        val syntheticClause =
          for {
            tpClause <- enumParent.typeParametersClause
            tpText   = tpClause.getTextByStub
          } yield
            ScalaPsiElementFactory.createTypeParameterClauseFromTextWithContext(tpText, this, this.nameId)

        syntheticClause.fold(Seq.empty[ScTypeParam])(_.typeParameters)
      } catch {
        case p: ProcessCanceledException => throw p
        case NonFatal(_)                 => Seq.empty
      }
    } else super.typeParameters
  }

  private def syntheticEnumClass: Option[ScTypeDefinition] =
    enumParent.syntheticClass

  override def superTypes: List[ScType] =
    if (extendsBlock.templateParents.nonEmpty) super.superTypes
    else {
      syntheticEnumClass match {
        case Some(cls) =>
          val tps = cls.typeParameters
          if (tps.isEmpty) List(ScalaType.designator(cls))
          else {
            if (constructor.isEmpty) {
              val tpBounds = cls.typeParameters.map(tp =>
                if (tp.isCovariant)          tp.lowerBound.getOrNothing
                else if (tp.isContravariant) tp.upperBound.getOrAny
                else                         Nothing
              )

              List(ParameterizedType(ScDesignatorType(cls), tpBounds))
            } else List(ParameterizedType(ScDesignatorType(cls), typeParameters.map(TypeParameterType(_))))
          }
        case None => List.empty
      }
    }

  override def supers: Seq[PsiClass] =
    if (extendsBlock.templateParents.nonEmpty) super.supers
    else                                       syntheticEnumClass.toSeq

  override protected def targetTokenType: ScalaTokenType = kCASE

  override protected def baseIcon: Icon = Icons.ENUM

  override def isLocal: Boolean = false

  override def getSyntheticCounterpart: ScNamedElement = {
    val res = enumParent.syntheticClass.flatMap(ScalaPsiUtil.getCompanionModule).toSeq
      .flatMap(_.syntheticMembers).collectFirst {
      case c: ScClass if c.name == name => c
      case p: ScPatternDefinition if p.declaredElements.exists(_.name == name) => p.declaredElements.find(_.name == name).get
    }

    if (res.isEmpty) Log.debug(
      s"""Failed to find a synthetic counterpart of $this. Each ScEnumCase should have exactly one.
         |Check if EnumMembersInjector is doing its job correctly.
         |""".stripMargin)

    res.getOrElse(this)
  }

  override def delete(): Unit = {
    val enumCasesElements = getContext.asOptionOf[ScEnumCases].toSeq.flatMap(_.declaredElements)
    val isOnlyCaseInEnumCases = enumCasesElements.size == 1
    val isRightmostInEnumCases = enumCasesElements.lastOption.contains(this)

    def findStart(): Option[PsiElement] = this.prevSiblings.takeWhile { e =>
        (isRightmostInEnumCases && (e.isWhitespace || e.elementType == ScalaTokenTypes.tCOMMA)) ||
          (isOnlyCaseInEnumCases && e.elementType == ScalaTokenTypes.kCASE)
      }.toSeq.lastOption

    def findEnd(): Option[PsiElement] = this.nextSiblings.takeWhile { e =>
        !isRightmostInEnumCases && (e.isWhitespace || e.elementType == ScalaTokenTypes.tCOMMA)
      }.toSeq.lastOption

    getContext.deleteChildRange(findStart().getOrElse(this), findEnd().getOrElse(this))
  }
}

object ScEnumCaseImpl {
  val Log: Logger = Logger.getInstance(getClass)
}