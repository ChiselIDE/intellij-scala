package org.jetbrains.plugins.scala.compiler.highlighting

import com.intellij.codeInsight.daemon.impl.HighlightInfoType
import org.jetbrains.jps.incremental.scala.Client.PosInfo
import org.jetbrains.jps.incremental.scala.TextEdit2
import org.jetbrains.plugins.scala.compiler.highlighting.ExternalHighlighting.PosRange
import xsbti.TextEdit

/**
 * All information that needed for highlighting.
 *
 * Note: potentially this class can be a sealed ADT.
 *
 * @param range can be None if a message was produce for a file, but no location was specified (for some reason)
 */
final case class ExternalHighlighting(highlightType: HighlightInfoType,
                                      message: String,
                                      range: Option[PosRange],
                                      quickFixes: List[TextEdit2] = Nil)

object ExternalHighlighting {

  final case class PosRange(from: Pos, to: Pos)

  sealed trait Pos
  
  object Pos {
    final case class LineColumn(line: Int, column: Int) extends Pos
    final case class Offset(offset: Int) extends Pos
    
    def fromPosInfo(posInfo: PosInfo): Option[Pos] =
      Option(posInfo).collect {
        // TODO The cases should be in different order. But the highlighting works incorrect with offset by some reason
        case PosInfo(Some(line), Some(column), _) => LineColumn(line.toInt, column.toInt)
        case PosInfo(_, _, Some(offset)) => Offset(offset.toInt)
      }
  }
}