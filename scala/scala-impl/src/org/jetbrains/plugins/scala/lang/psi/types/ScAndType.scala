package org.jetbrains.plugins.scala.lang.psi.types

import org.jetbrains.plugins.scala.extensions.ObjectExt
import org.jetbrains.plugins.scala.lang.psi.types.api.{ParameterizedType, TypeParameter, ValueType}
import org.jetbrains.plugins.scala.project.ProjectContext

/**
 * Scala 3 intersection type, e.g. `Foo & Bar`
 */
final case class ScAndType private(lhs: ScType, rhs: ScType) extends ScalaType with ValueType {
  override def visitType(visitor: ScalaTypeVisitor): Unit = visitor.visitAndType(this)

  override implicit def projectContext: ProjectContext = lhs.projectContext

  override def equivInner(
    r:           ScType,
    constraints: ConstraintSystem,
    falseUndef:  Boolean
  ): ConstraintsResult = r match {
    case ScAndType(rLhs, rRhs) =>
      if (r eq this) constraints
      else {
        val lhsConstraints = lhs.equiv(rLhs, constraints, falseUndef)

        lhsConstraints match {
          case ConstraintsResult.Left =>
            val swapped = lhs.equiv(rRhs, constraints, falseUndef)
            swapped match {
              case ConstraintsResult.Left => ConstraintsResult.Left
              case cs: ConstraintSystem   => rhs.equiv(rLhs, cs, falseUndef)
            }
          case cs: ConstraintSystem   => rhs.equiv(rRhs, cs, falseUndef)
        }
      }
    case _ => ConstraintsResult.Left
  }
}

object ScAndType {
  def apply(lhs: ScType, rhs: ScType): ValueType = {
    assert(lhs.isValue && rhs.isValue, "Components of an intersection type must be value types.")

    if (lhs == rhs || rhs.isAny) lhs.asInstanceOf[ValueType]
    else if (lhs.isAny)          rhs.asInstanceOf[ValueType]
    else                         makeAndType(lhs, rhs)
  }

  private[this] def checkEquiv(lhs: ScType, rhs: ScType): Boolean =
    lhs.equiv(rhs, ConstraintSystem.empty, falseUndef = false).isRight

  private[this] def makeAndType(lhs: ScType, rhs: ScType): ValueType = (lhs, rhs) match {
    case (ParameterizedType(des1, args1), ParameterizedType(des2, args2))
      if checkEquiv(des1, des2) =>
      val jointArgs = glbArgs(args1, args2, extractTypeParameters(des1))
      jointArgs.fold[ValueType](new ScAndType(lhs, rhs))(ScParameterizedType(des1, _))
    case _ => new ScAndType(lhs, rhs)
  }

  private[this] def glbArgs(
    args1:      Seq[ScType],
    args2:      Seq[ScType],
    typeParams: Seq[TypeParameter]
  ): Option[Seq[ScType]] = {
    val zippedArgs = args1.lazyZip(args2).lazyZip(typeParams).iterator
    val jointArgs  = Seq.newBuilder[ScType]

    while (zippedArgs.hasNext) {
      val (arg1, arg2, typeParam) = zippedArgs.next()

      if (checkEquiv(arg1, arg2))         jointArgs += arg1
      else if (typeParam.isCovariant)     jointArgs += arg1.glb(arg2)
      else if (typeParam.isContravariant) jointArgs += arg1.lub(arg2)
      else                                return None
    }

    jointArgs.result().toOption
  }
}
