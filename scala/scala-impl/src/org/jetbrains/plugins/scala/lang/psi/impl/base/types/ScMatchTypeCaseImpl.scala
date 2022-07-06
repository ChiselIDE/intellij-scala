package org.jetbrains.plugins.scala.lang.psi.impl.base.types

import com.intellij.lang.ASTNode
import org.jetbrains.plugins.scala.lang.psi.api.base.types.{ScMatchTypeCase, ScTypeElement}
import org.jetbrains.plugins.scala.lang.psi.impl.ScalaPsiElementImpl

class ScMatchTypeCaseImpl(node: ASTNode) extends ScalaPsiElementImpl(node) with ScMatchTypeCase {

  override def patternTypeElement: Option[ScTypeElement] = findChild[ScTypeElement]

  override def resultTypeElement: Option[ScTypeElement] = findLastChild[ScTypeElement]
}
