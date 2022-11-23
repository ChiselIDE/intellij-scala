package org.jetbrains.plugins.scala.lang.parser.parsing.expressions

import com.intellij.lang.PsiBuilder
import org.jetbrains.plugins.scala.lang.lexer.{ScalaTokenType, ScalaTokenTypes}
import org.jetbrains.plugins.scala.lang.parser.parsing.builder.ScalaPsiBuilder
import org.jetbrains.plugins.scala.lang.parser.util.InScala3
import org.jetbrains.plugins.scala.lang.parser.{ErrMsg, ScalaElementType}

import scala.util.chaining.scalaUtilChainingOps

/*
 * ResultExpr ::=  Expr1
 *              |  (Bindings | id ':' CompoundType) '=>' Block
 *
 * In Scala 3:
 *
 * BlockResult ::=  [‘implicit’] FunParams ‘=>’ Block
 *               |  Expr1
 */
object ResultExpr {
  def apply(stopOnOutdent: Boolean)(implicit builder: ScalaPsiBuilder): Boolean = {
    val resultMarker = builder.mark()
    val backupMarker = builder.mark()

    def parseFunctionEnd(): Boolean = builder.getTokenType match {
      case ScalaTokenTypes.tFUNTYPE | ScalaTokenType.ImplicitFunctionArrow =>
        builder.advanceLexer() //Ate => or ?=>
        Block.Braceless(stopOnOutdent, needNode = true).tap { parsedBlock =>
          if (parsedBlock) {
            backupMarker.drop()
            resultMarker.done(ScalaElementType.FUNCTION_EXPR)
          } else {
            resultMarker.drop()
            backupMarker.rollbackTo()
          }
        }
      case _ =>
        resultMarker.drop()
        backupMarker.rollbackTo()
        false
    }

    def parseFunction(paramsMarker: PsiBuilder.Marker): Boolean = {
      val paramMarker = parseParam()
      builder.getTokenType match {
        case ScalaTokenTypes.tFUNTYPE | ScalaTokenType.ImplicitFunctionArrow =>
          completeParamClauses(paramMarker)(paramsMarker)
          return parseFunctionEnd()
        case _ =>
          builder error ErrMsg("fun.sign.expected")
      }
      parseFunctionEnd()
    }

    builder.getTokenType match {
      case ScalaTokenTypes.tLPARENTHESIS =>
        Bindings()
        return parseFunctionEnd()
      case ScalaTokenTypes.kIMPLICIT =>
        val pmarker = builder.mark()
        builder.advanceLexer() //ate implicit
        builder.getTokenType match {
          case ScalaTokenTypes.tIDENTIFIER =>
            return parseFunction(pmarker)
          case InScala3(ScalaTokenTypes.tLPARENTHESIS) =>
            pmarker.drop()
            Bindings()
            return parseFunctionEnd()
          case _ =>
            resultMarker.drop()
            backupMarker.rollbackTo()
            return false
        }
      case ScalaTokenTypes.tIDENTIFIER | ScalaTokenTypes.tUNDER =>
        val pmarker = builder.mark()
        return parseFunction(pmarker)
      case _ =>
        backupMarker.drop()
    }
    resultMarker.drop()
    false
  }
}
