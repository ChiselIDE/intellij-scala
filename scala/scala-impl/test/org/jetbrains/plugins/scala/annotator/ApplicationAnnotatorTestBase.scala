package org.jetbrains.plugins.scala
package annotator


import org.intellij.lang.annotations.Language
import org.jetbrains.plugins.scala.annotator.element.ElementAnnotator
import org.jetbrains.plugins.scala.extensions._
import org.jetbrains.plugins.scala.lang.psi.api.ScalaPsiElement
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.ScClass
import org.jetbrains.plugins.scala.lang.psi.types.Compatibility

abstract class ApplicationAnnotatorTestBase extends AnnotatorSimpleTestCase {
  final val Header =
"""
class Seq[+A]
object Seq {
  def apply[A](a: A) = new Seq[A]
}
class A
class B
object A extends A
object B extends B
"""


  def messages(@Language(value = "Scala", prefix = Header) code: String): List[Message] = {
    val file = (Header + code).parse

    implicit val mock: AnnotatorHolderMock = new AnnotatorHolderMock(file)

    val seq = file.depthFirst().findByType[ScClass]
    Compatibility.seqClass = seq
    try {
      // TODO use the general annotate() method
      file.depthFirst().filterByType[ScalaPsiElement].foreach {
        ElementAnnotator.annotate(_, typeAware = true)
      }

      mock.annotations
    }
    finally {
      Compatibility.seqClass = None
    }
  }
}
