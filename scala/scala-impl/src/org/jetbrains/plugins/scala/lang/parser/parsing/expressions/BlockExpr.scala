package org.jetbrains.plugins.scala.lang.parser.parsing.expressions

import org.jetbrains.plugins.scala.lang.parser.{BlockIndentation, ScCodeBlockElementType}
import org.jetbrains.plugins.scala.lang.parser.parsing.ParsingRule
import org.jetbrains.plugins.scala.lang.parser.parsing.builder.ScalaPsiBuilder
import org.jetbrains.plugins.scala.lang.parser.parsing.patterns.CaseClauses
import org.jetbrains.plugins.scala.lang.parser.util.ParserUtils

/*
 * BlockExpr ::= '{' CaseClauses '}'
 *             | '{' Block '}'
 */
object BlockExpr extends ParsingRule {

  import org.jetbrains.plugins.scala.lang.lexer.ScalaTokenType._
  import org.jetbrains.plugins.scala.lang.lexer.ScalaTokenTypes._

  override def parse(implicit builder: ScalaPsiBuilder): Boolean = {
    if (builder.skipExternalToken()) return true

    val blockExprMarker = builder.mark()
    builder.getTokenType match {
      case `tLBRACE` =>
        builder.advanceLexer()
        builder.enableNewlines()
        builder.enterBracedRegion()
      case _ =>
        blockExprMarker.drop()
        return false
    }
    val blockIndentation = BlockIndentation.create
    ParserUtils.parseLoopUntilRBrace() {
      builder.getTokenType match {
        case `kCASE` =>
          val backMarker = builder.mark()
          builder.advanceLexer()
          builder.getTokenType match {
            case ClassKeyword | ObjectKeyword =>
              backMarker.rollbackTo()
              Block.ContentInBraces()
            case _ =>
              backMarker.rollbackTo()
              CaseClauses()
          }
        case _ =>
          blockIndentation.fromHere()
          Block.ContentInBraces()
      }
    }
    blockIndentation.drop()
    builder.exitBracedRegion()
    builder.restoreNewlinesState()
    blockExprMarker.done(ScCodeBlockElementType.BlockExpression)
    true
  }
}
