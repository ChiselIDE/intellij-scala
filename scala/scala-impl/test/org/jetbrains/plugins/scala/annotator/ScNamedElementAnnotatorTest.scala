package org.jetbrains.plugins.scala.annotator

class ScNamedElementAnnotatorTest extends ScalaHighlightingTestBase {
  def testOverrideParameter(): Unit = {
    val scalaCode =
      """
        |trait Animal { var cat: String }
        |
        |class Cat(override val cat: String) extends Animal
        |""".stripMargin

    val errors = errorsFromScalaCode(scalaCode)
    assert(errors.exists(err => err.element == "override val cat: String"))
  }

  def testOverrideVal(): Unit = {
    val scalaCode =
      """
        |trait Animal { var cat: String }
        |
        |class Cat extends Animal {
        |  override val cat: String = ""
        |}
        |""".stripMargin

    val errors = errorsFromScalaCode(scalaCode)
    assert(errors.exists(err => err.element == "cat"))
  }

  def testOverrideParamByVal(): Unit = {
    val scalaCode =
      """
        |class Animal(var cat: String = "")
        |
        |class Cat extends Animal {
        |  override val cat: String = ""
        |}
        |""".stripMargin

    val errors = errorsFromScalaCode(scalaCode)
    assert(errors.exists(err => err.element == "cat"))
  }

  def testOverrideParamByParam(): Unit = {
    val scalaCode =
      """
        |class Animal(var cat: String)
        |
        |class Cat(override val cat: String) extends Animal(cat)
        |""".stripMargin

    val errors = errorsFromScalaCode(scalaCode)
    assert(errors.exists(err => err.element == "override val cat: String"))
  }

  def testOverrideParamByVar(): Unit = {
    val scalaCode =
      """
        |class Animal(var cat: String)
        |
        |class Cat extends Animal("") {
        |  override var cat: String = "cat"
        |}
        |""".stripMargin

    val errors = errorsFromScalaCode(scalaCode)
    assert(errors.exists(err => err.element == "cat"))
  }

  def testOverrideParamByDef(): Unit = {
    val scalaCode =
      """
        |class Animal(var cat: String)
        |
        |class Cat extends Animal("") {
        |  override def cat: String = "cat"
        |}
        |""".stripMargin

    val errors = errorsFromScalaCode(scalaCode)
    assert(errors.exists(err => err.element == "cat"))
  }

  def testOverrideAbstractVarByVarIsOk(): Unit = {
    val scalaCode =
      """
        |trait Animal {
        |  var cat: String
        |}
        |
        |class Cat extends Animal {
        |  override var cat: String = ""
        |}
        |""".stripMargin

    val errors = errorsFromScalaCode(scalaCode)
    assert(errors.isEmpty)
  }

  def testOverrideVarByValRequiresSetter(): Unit = {
    val scalaCode =
      """
        |trait Animal {
        |  var cat: String
        |}
        |
        |class Cat extends Animal {
        |  override val cat: String = ""
        |  def cat_=(x: String): Unit = {}
        |}
        |""".stripMargin

    val errors = errorsFromScalaCode(scalaCode)
    assert(errors.isEmpty)
  }

  def testOverrideModifierNotAllowedAtTopLevelDefinitions(): Unit = {
    assertErrorsText(
      """override def foo1: String = ???
        |override val foo2: String = ???
        |override var foo3: String = ???
        |""".stripMargin,
      """Error(override,'override' modifier is not allowed here)
        |Error(override,'override' modifier is not allowed here)
        |Error(override,'override' modifier is not allowed here)
        |""".stripMargin
    )
  }
}
