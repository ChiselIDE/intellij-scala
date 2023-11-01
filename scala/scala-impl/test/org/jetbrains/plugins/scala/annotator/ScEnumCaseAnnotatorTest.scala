package org.jetbrains.plugins.scala.annotator

import org.jetbrains.plugins.scala.{LatestScalaVersions, ScalaVersion}

class ScEnumCaseAnnotatorTest extends ScalaHighlightingTestBase {
  import Message._

  override protected def supportedIn(version: ScalaVersion): Boolean =
    version >= LatestScalaVersions.Scala_3_0

  private def doTest(text: String)(expectedErrors: Message*): Unit = {
    val errors = errorsFromScalaCode(text)
    assertMessages(errors)(expectedErrors: _*)
  }

  private def doTestInWorksheet(text: String)(expectedErrors: Message*): Unit = {
    val errors = errorsFromScalaCode(text, "worksheet.sc")
    assertMessages(errors)(expectedErrors: _*)
  }

  def testCreateBaseClassInstance(): Unit =
    doTest(
      """
        |object Test {
        |  enum Foo { case Bar }
        |  val f: Foo = new Foo {}
        |}
        |""".stripMargin
    )(
      // TODO Show only a single error
      Error("Foo", "Extending enums is prohibited"),
      Error("Foo", "Object creation impossible, since member ordinal: Int in scala.reflect.Enum is not defined")
    )

  def testInheritFromEnumClass(): Unit =
    doTest(
      """
        |object Test {
        |  enum Foo { case Bar }
        |  class X extends Foo
        |}
        |""".stripMargin
    )(
      // TODO Show only a single error
      Error("Foo", "Extending enums is prohibited"),
      Error("class X extends Foo", "Class 'X' must either be declared abstract or implement abstract member 'ordinal: Int' in 'scala.reflect.Enum'")
    )

  def testNonVariantTypeParameterNeg(): Unit =
    doTest(
      """
        |enum Foo[T] {
        |  case F
        |}
        |""".stripMargin
    )(Error("F", "Cannot determine type argument for enum class parent Foo, type parameter T is invariant"))

  def testNonVariantTypeParameterPos(): Unit =
    doTest(
      """
        |enum Option[T] {
        |  case Some(x: T) extends Option[T]
        |  case None       extends Option[Nothing]
        |}
        |""".stripMargin
    )()

  def testRequiresExtendsParent(): Unit =
    doTest(
      """
        |trait F
        |enum Option[+T] {
        |  case Some(x: T) extends F
        |  case None
        |}
        |""".stripMargin
    )(Error("extends F", "Enum case must extend its enum class Option"))

  def testWithExplicitExtendsParent(): Unit =
    doTest(
      """
        |trait F
        |enum Option[+T] {
        |  case Some(x: T) extends Option[T]       with F
        |  case None       extends Option[Nothing] with F
        |}
        |""".stripMargin
    )()

  def testWithExplicitExtendsParent_TopLevel(): Unit =
    assertNoErrors(
      """enum ToplevelEnum:
        |  case Case extends ToplevelEnum""".stripMargin
    )

  def testWithExplicitExtendsParent_InsideFunction(): Unit =
    assertNoErrors(
      """def foo(): Unit =
        |  enum EnumInsideDef:
        |    case Case extends EnumInsideDef""".stripMargin
    )

  def testWithExplicitExtendsParent_InsideObject(): Unit =
    assertNoErrors(
      """object Enums:
        |  enum EnumInsideObj:
        |    case Case extends EnumInsideObj""".stripMargin
    )

  def testWithExplicitExtendsParent_InWorksheet_TopLevel(): Unit =
    doTestInWorksheet(
      """enum Move(val score: Int) {
        |  case Rock     extends Move(1)
        |  case Paper    extends Move(2)
        |  case Scissors extends Move(3)
        |}
        |""".stripMargin
    )()

  def testWithExplicitExtendsParent_InWorksheet_Inner(): Unit =
    doTestInWorksheet(
      """object wrapper {
        |  enum Move(val score: Int) {
        |    case Rock     extends Move(1)
        |    case Paper    extends Move(2)
        |    case Scissors extends Move(3)
        |  }
        |}
        |""".stripMargin
    )()

  def testExplicitTypeParameterBlock(): Unit =
    doTest(
      """
        |enum Foo[+T] {
        |  case Bar[Y](y: Y)
        |}
        |""".stripMargin
    )(Error("Bar", "Explicit extends clause required because both enum case and enum class have type parameters"))

  def testExplicitTypeParametersWithExplicitExtends(): Unit =
    doTest(
      """
        |enum Foo[+T] {
        |  case Bar[Y](y: Y) extends Foo[Y]
        |}
        |""".stripMargin
    )()

  def testSCL21387(): Unit = doTest(
    """
      |enum Color { case Green(x: Int) }
      |
      |class C extends Color.Green(1)
      |""".stripMargin
  )(
    // TODO Show only a single error
    Error("Color.Green", "Illegal inheritance from final class 'Green'"),
    Error("class C extends Color.Green(1)", "Class 'C' must either be declared abstract or implement abstract member 'ordinal: Int' in 'scala.reflect.Enum'")
  )
}
