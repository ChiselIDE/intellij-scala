package org.jetbrains.plugins.scala.lang.parser.parsing.expressions

import com.intellij.lang.PsiBuilder
import com.intellij.psi.tree.IElementType
import org.jetbrains.plugins.scala.lang.lexer.ScalaTokenType.ImplicitFunctionArrow
import org.jetbrains.plugins.scala.lang.lexer.ScalaTokenTypes.{tFUNTYPE, tLSQBRACKET}
import org.jetbrains.plugins.scala.lang.lexer.{ScalaTokenType, ScalaTokenTypes}
import org.jetbrains.plugins.scala.lang.parser.ScalaElementType.{ARG_EXPRS, FUNCTION_EXPR, POLY_FUNCTION_EXPR}
import org.jetbrains.plugins.scala.lang.parser.parsing.ParsingRule
import org.jetbrains.plugins.scala.lang.parser.parsing.builder.ScalaPsiBuilder
import org.jetbrains.plugins.scala.lang.parser.parsing.params.TypeParamClause
import org.jetbrains.plugins.scala.lang.parser.parsing.patterns.CaseClausesWithoutBraces
import org.jetbrains.plugins.scala.lang.parser.util.InBracelessScala3
import org.jetbrains.plugins.scala.lang.parser.{IndentationWidth, ScCodeBlockElementType, ScalaElementType}

import scala.util.chaining.scalaUtilChainingOps

/**
 * Scala 3: {{{
 * ColonArgument  ::= colon [LambdaStart]
 *                    indent (CaseClauses | Block) outdent
 *
 * LambdaStart    ::= FunParams ('=>' | '?=>')
 *                  | HkTypeParamClause '=>'
 * }}}
 */
object ColonArgument extends ParsingRule {

  override def parse(implicit builder: ScalaPsiBuilder): Boolean =
    builder.getTokenType match {
      case InBracelessScala3(ScalaTokenTypes.tCOLON) if builder.findPreviousNewLine.isEmpty =>
        val argMarker = builder.mark()
        val blockExprMarker = builder.mark()

        val parsed = if (followingIsLambdaAfterColon) {
          builder.advanceLexer() // ate ':'

          val nodeType =
            if (builder.getTokenType == tLSQBRACKET) POLY_FUNCTION_EXPR
            else FUNCTION_EXPR
          parseFunctionExpression(nodeType)
        } else {
          builder.advanceLexer() // ate ':'

          // if there is no lambda after ':' then there must be a newline
          builder.findPreviousNewLine.isDefined &&
            parseCaseClausesOrBlock(builder.findPreviousIndent)
        }

        if (parsed) {
          blockExprMarker.done(ScCodeBlockElementType.BlockExpression)
          argMarker.done(ARG_EXPRS)
        }
        else {
          blockExprMarker.drop()
          argMarker.rollbackTo()
        }

        parsed
      case _ =>
        false
    }

  /**
   * {{{ CaseClauses | Block }}}
   */
  private def parseCaseClausesOrBlock(indentation: Option[IndentationWidth])
                                     (implicit builder: ScalaPsiBuilder): Boolean =
    builder.withEnabledNewlines {
      builder.maybeWithIndentationWidth(indentation) {
        builder.getTokenType match {
          case ScalaTokenTypes.kCASE =>
            val backMarker = builder.mark()
            builder.advanceLexer()
            builder.getTokenType match {
              case ScalaTokenType.ClassKeyword | ScalaTokenType.ObjectKeyword =>
                backMarker.rollbackTo()
                Block.Braceless(stopOnOutdent = true)
              case _ =>
                backMarker.rollbackTo()
                CaseClausesWithoutBraces()
            }
          case _ =>
            Block.Braceless(stopOnOutdent = true)
        }
      }
    }

  /**
   * {{{ LambdaStart (CaseClauses | Block) }}}
   */
  private def parseFunctionExpression(nodeType: ScalaElementType)
                                     (implicit builder: ScalaPsiBuilder): Boolean = {
    val funExprMarker = builder.mark()
    val indentAfterColon = builder.findPreviousIndent

    def rollback(additionalMarkersToDrop: PsiBuilder.Marker*): Boolean = {
      additionalMarkersToDrop.foreach(_.drop())
      funExprMarker.rollbackTo()
      false
    }

    def parseBlockAfterArrow(expectedArrows: IElementType*): Boolean =
      if (expectedArrows.contains(builder.getTokenType)) {
        builder.advanceLexer() // ate `=>` or `?=>`
        val indentAfterArrow = builder.findPreviousIndent
        // cannot have one-line lambda like `.foo: x => x + 1`
        val isOk = indentAfterColon.isDefined || indentAfterArrow.isDefined

        // TODO: if not isOk add "newline expected" error and parse block
        if (isOk) {
          parseCaseClausesOrBlock(indentAfterArrow).tap { parsedBlock =>
            if (parsedBlock) funExprMarker.done(nodeType)
            else rollback()
          }
        } else rollback()
      } else rollback()

    builder.maybeWithIndentationWidth(indentAfterColon) {
      builder.getTokenType match {
        case ScalaTokenTypes.tLPARENTHESIS =>
          Bindings() &&
            parseBlockAfterArrow(tFUNTYPE, ImplicitFunctionArrow)
        case ScalaTokenTypes.tLSQBRACKET =>
          TypeParamClause(mayHaveViewBounds = false, mayHaveContextBounds = false) &&
            parseBlockAfterArrow(tFUNTYPE)
        case ScalaTokenTypes.tIDENTIFIER | ScalaTokenTypes.tUNDER =>
          val paramMarker = parseParam()

          builder.getTokenType match {
            case ScalaTokenTypes.tFUNTYPE | ImplicitFunctionArrow =>
              completeParamClauses(paramMarker)()
              parseBlockAfterArrow(tFUNTYPE, ImplicitFunctionArrow)
            case _ => rollback(paramMarker)
          }
        case _ =>
          rollback()
      }
    }
  }

  /** Is the token sequence following the current `:` token classified as a lambda?
   * This is the case if the input starts with an identifier, a wildcard, or
   * something enclosed in (...) or [...], and this is followed by a `=>` or `?=>`.
   */
  private def followingIsLambdaAfterColon(implicit b: ScalaPsiBuilder): Boolean =
    b.predict { builder =>
      def followingIsArrow: Boolean = builder.getTokenType match {
        case ScalaTokenTypes.tFUNTYPE | ImplicitFunctionArrow => true
        case _ => false
      }

      builder.getTokenType match {
        case ScalaTokenTypes.tIDENTIFIER | ScalaTokenTypes.tUNDER =>
          builder.advanceLexer()
          followingIsArrow
        case ScalaTokenTypes.tLPARENTHESIS | ScalaTokenTypes.tLSQBRACKET =>
          builder.skipParensOrBrackets()
          followingIsArrow
        case _ => false
      }
    }
}
