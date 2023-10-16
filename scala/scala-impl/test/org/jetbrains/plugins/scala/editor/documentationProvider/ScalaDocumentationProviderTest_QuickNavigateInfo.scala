package org.jetbrains.plugins.scala.editor.documentationProvider

import org.jetbrains.plugins.scala.util.assertions.StringAssertions._

class ScalaDocumentationProviderTest_QuickNavigateInfo extends ScalaDocumentationProviderTestBase {

  private def moduleName: String = getModule.getName

  def testSimpleClass(): Unit =
    doGenerateQuickDocTest(
      s"""class ${|}MyClass""",
      s"""[$moduleName] default
         |class MyClass extends <span style="color:#000000;"><a href="psi_element://java.lang.Object"><code>Object</code></a></span>
         |""".stripMargin
    )

  def testSimpleClassParam(): Unit =
    doGenerateQuickDocTest(
      s"""class MyClass(s${|}tr: String)""",
      s"""MyClass <default>
         |str: <span style="color:#000000;"><a href="psi_element://java.lang.String"><code>String</code></a></span>
         |""".stripMargin
    )

  def testSimpleClassParamWithDefaultValue(): Unit =
    doGenerateQuickDocTest(
      s"""class MyClass(s${|}tr: String = "default value")""",
      s"""MyClass <default>
         |str: <span style="color:#000000;"><a href="psi_element://java.lang.String"><code>String</code></a></span> = …
         |""".stripMargin
    )

  def testSimpleTypeAlias(): Unit =
    doGenerateQuickDocTest(
      s"""type ${|}Foo = String""",
      s"""type Foo = <span style="color:#000000;"><a href="psi_element://java.lang.String"><code>String</code></a></span>
         |""".stripMargin
    )

  def testSimpleTrait(): Unit =
    doGenerateQuickDocTest(
      s"""trait ${|}MyTrait""",
      s"""[$moduleName] default
         |trait MyTrait""".stripMargin
    )

  def testSimpleObject(): Unit =
    doGenerateQuickDocTest(
      s"""object ${|}MyObject""",
      s"""[$moduleName] default
         |object MyObject extends <span style="color:#000000;"><a href="psi_element://java.lang.Object"><code>Object</code></a></span>
         |""".stripMargin
    )

  def testClassWithModifiers(): Unit =
    doGenerateQuickDocTest(
      s"""abstract sealed class ${|}MyClass""",
      s"""[$moduleName] default
         |abstract sealed class MyClass extends <span style="color:#000000;"><a href="psi_element://java.lang.Object"><code>Object</code></a></span>
         |""".stripMargin
    )

  def testClassWithModifiers_1(): Unit =
    doGenerateQuickDocTest(
      s"""final class ${|}MyClass""",
      s"""[$moduleName] default
         |final class MyClass extends <span style="color:#000000;"><a href="psi_element://java.lang.Object"><code>Object</code></a></span>
         |""".stripMargin
    )

  def testClassWithGenericParameter(): Unit =
    doGenerateQuickDocTest(
      s"""class ${|}Class[T]""",
      s"""[$moduleName] default
         |class Class[T] extends <span style="color:#000000;"><a href="psi_element://java.lang.Object"><code>Object</code></a></span>
         |""".stripMargin
    )

  def testClassWithGenericParameter_WithBounds(): Unit =
    doGenerateQuickDocTest(
      s"""trait Trait[A]
         |class ${|}Class[T <: Trait[_ >: Object]]
         |""".stripMargin,
      s"""[$moduleName] default
         |class Class[T &lt;: <span style="color:#000000;"><a href="psi_element://Trait"><code>Trait</code></a></span>[_ &gt;: <span style="color:#000000;"><a href="psi_element://java.lang.Object"><code>Object</code></a></span>]] extends <span style="color:#000000;"><a href="psi_element://java.lang.Object"><code>Object</code></a></span>
         |""".stripMargin
    )

  def testClassWithGenericParameter_WithRecursiveBounds(): Unit =
    doGenerateQuickDocTest(
      s"""trait Trait[T]
         |class ${|}Class2[T <: Trait[T]]
         |""".stripMargin,
      s"""[$moduleName] default
         |class Class2[T &lt;: <span style="color:#000000;"><a href="psi_element://Trait"><code>Trait</code></a></span>[T]] extends <span style="color:#000000;"><a href="psi_element://java.lang.Object"><code>Object</code></a></span>
         |""".stripMargin
    )

  def testClassWithGenericParameter_WithRecursiveBounds_1(): Unit =
    doGenerateQuickDocTest(
      s"""trait Trait[T]
         |class ${|}Class4[T <: Trait[_ >: Trait[T]]]
         |""".stripMargin,
      s"""[$moduleName] default
         |class Class4[T &lt;: <span style="color:#000000;"><a href="psi_element://Trait"><code>Trait</code></a></span>[_ &gt;: <span style="color:#000000;"><a href="psi_element://Trait"><code>Trait</code></a></span>[T]]] extends <span style="color:#000000;"><a href="psi_element://java.lang.Object"><code>Object</code></a></span>
         |""".stripMargin
    )

  def testClassWithSuperWithGenerics(): Unit =
    doGenerateQuickDocTest(
      s"""trait Trait[A]
         |abstract class ${|}Class extends Comparable[_ <: Trait[_ >: String]]
         |""".stripMargin,
      s"""[$moduleName] default
         |abstract class Class extends <span style="color:#000000;"><a href="psi_element://java.lang.Comparable"><code>Comparable</code></a></span>[_ &lt;: <span style="color:#000000;"><a href="psi_element://Trait"><code>Trait</code></a></span>[_ &gt;: <span style="color:#000000;"><a href="psi_element://java.lang.String"><code>String</code></a></span>]]
         |""".stripMargin
    )

  def testClassExtendsListShouldNotContainWithObject(): Unit = {
    myFixture.addFileToProject("commons.scala",
      """class BaseClass
        |trait BaseTrait1
        |trait BaseTrait2
        |""".stripMargin
    )
    val classesWithoutObject = Seq(
      s"class ${|}MyClass2 extends BaseClass",
      s"class ${|}MyClass4 extends BaseTrait1",
      s"class ${|}MyClass3 extends BaseClass with BaseTrait1",
      s"class ${|}MyTrait1",
      s"class ${|}MyTrait2 extends BaseTrait1",
      s"class ${|}MyTrait3 extends BaseTrait1 with BaseTrait2"
    )
    // testing exact quick info value would be very noisy, it's enough to test just presence of ` with Object` which can be escaped!
    val withObjectRegex      = "(\\s|\\n)with .*Object".r
    classesWithoutObject.foreach { content =>
      val quickInfo = generateQuickDoc(content)
      assertStringNotMatches(quickInfo, withObjectRegex)
    }
  }

  def testClassExtendsListShouldContainObjectIfThereAreNoBaseClasses(): Unit = {
    myFixture.addFileToProject("commons.scala",
      """class BaseClass
        |trait BaseTrait1
        |trait BaseTrait2
        |""".stripMargin
    )

    val classesWithObject = Seq(
      s"class ${|}MyClass1"
    )

    val extendsObjectRegex = "(\\s|\\n)extends .*Object".r
    classesWithObject.foreach { content =>
      val quickInfo = generateQuickDoc(content)
      assertStringMatches(quickInfo, extendsObjectRegex)
    }
  }

  def testValueDefinition(): Unit =
    doGenerateQuickDocTest(
      s"""class Wrapper {
         |  val (field1, ${|}field2) = (42, "hello")
         |}""".stripMargin,
      """<span style="color:#000000;"><a href="psi_element://Wrapper"><code>Wrapper</code></a></span> <default>
        |val field2: <span style="color:#000000;"><a href="psi_element://java.lang.String"><code>String</code></a></span> = (42, "hello")
        |""".stripMargin
    )

  def testValueDeclaration(): Unit =
    doGenerateQuickDocTest(
      s"""abstract class Wrapper {
         |  val ${|}field2: String
         |}""".stripMargin,
      s"""<span style="color:#000000;"><a href="psi_element://Wrapper"><code>Wrapper</code></a></span> <default>
         |val field2: <span style="color:#000000;"><a href="psi_element://java.lang.String"><code>String</code></a></span>
         |""".stripMargin
    )

  def testVariableDefinition(): Unit =
    doGenerateQuickDocTest(
      s"""class Wrapper {
         |  var (field1, ${|}field2) = (42, "hello")
         |}""".stripMargin,
      """<span style="color:#000000;"><a href="psi_element://Wrapper"><code>Wrapper</code></a></span> <default>
        |var field2: <span style="color:#000000;"><a href="psi_element://java.lang.String"><code>String</code></a></span> = (42, "hello")
        |""".stripMargin
    )

  def testVariableDeclaration(): Unit =
    doGenerateQuickDocTest(
      s"""abstract class Wrapper {
         |  var ${|}field2: String
         |}""".stripMargin,
      s"""<span style="color:#000000;"><a href="psi_element://Wrapper"><code>Wrapper</code></a></span> <default>
         |var field2: <span style="color:#000000;"><a href="psi_element://java.lang.String"><code>String</code></a></span>
         |""".stripMargin
    )

  def testValueWithModifiers(): Unit =
    doGenerateQuickDocTest(
      s"""class Wrapper {
         |  protected final lazy val ${|}field2 = "hello"
         |}""".stripMargin,
      """<span style="color:#000000;"><a href="psi_element://Wrapper"><code>Wrapper</code></a></span> <default>
        |protected final lazy val field2: <span style="color:#000000;"><a href="psi_element://java.lang.String"><code>String</code></a></span> = "hello"
        |""".stripMargin
    )

  def testHigherKindedTypeParameters(): Unit =
    doGenerateQuickDocTest(
      s"""object O {
         |  def ${|}f[A[_, B]] = 42
         |}""".stripMargin,
      """<span style="color:#000000;"><a href="psi_element://O"><code>O</code></a></span> <default>
        |def f[A[_, B]]: <span style="color:#000000;"><a href="psi_element://scala.Int"><code>Int</code></a></span>""".stripMargin
    )

  def testHigherKindedTypeParameters_1(): Unit =
    doGenerateQuickDocTest(
      s"""trait ${|}T[X[_, Y[_, Z]]]
         |""".stripMargin,
      """[light_idea_test_case] default
        |trait T[X[_, Y[_, Z]]]""".stripMargin
    )

  def testMethod(): Unit =
    doGenerateQuickDocTest(
      s"""class X {
         | def ${|}f1 = 42
         |}
         |""".stripMargin,
      """<span style="color:#000000;"><a href="psi_element://X"><code>X</code></a></span> <default>
        |def f1: <span style="color:#000000;"><a href="psi_element://scala.Int"><code>Int</code></a></span>
        |""".stripMargin
    )

  def testMethodWithAccessModifier(): Unit =
    doGenerateQuickDocTest(
      s"""class X {
         |  protected def ${|}f1 = 42
         |}
         |""".stripMargin,
      """<span style="color:#000000;"><a href="psi_element://X"><code>X</code></a></span> <default>
        |protected def f1: <span style="color:#000000;"><a href="psi_element://scala.Int"><code>Int</code></a></span>""".stripMargin
    )

  def testMethodWithAccessModifierWithThisQualifier(): Unit =
    doGenerateQuickDocTest(
      s"""class X {
         |  protected[this] def ${|}f1 = 42
         |}
         |""".stripMargin,
      """<span style="color:#000000;"><a href="psi_element://X"><code>X</code></a></span> <default>
        |protected def f1: <span style="color:#000000;"><a href="psi_element://scala.Int"><code>Int</code></a></span>""".stripMargin
    )

  def testTypeWithColon(): Unit =
    doGenerateQuickDocTest(
      s"""trait MyTrait[T]
         |class :::[T1, T2]
         |class ${|}ClassWithGenericColons1[A <: MyTrait[:::[Int, String]]]
         |  extends MyTrait[Int ::: String]
         |""".stripMargin,
      s"""[light_idea_test_case] default
         |class ClassWithGenericColons1[A &lt;: <span style="color:#000000;"><a href="psi_element://MyTrait"><code>MyTrait</code></a></span>[<span style="color:#000000;"><a href="psi_element://scala.Int"><code>Int</code></a></span> <span style="color:#000000;"><a href="psi_element://:::"><code>:::</code></a></span> <span style="color:#000000;"><a href="psi_element://java.lang.String"><code>String</code></a></span>]] extends <span style="color:#000000;"><a href="psi_element://MyTrait"><code>MyTrait</code></a></span>[<span style="color:#000000;"><a href="psi_element://scala.Int"><code>Int</code></a></span> <span style="color:#000000;"><a href="psi_element://:::"><code>:::</code></a></span> <span style="color:#000000;"><a href="psi_element://java.lang.String"><code>String</code></a></span>]""".stripMargin
    )
}
