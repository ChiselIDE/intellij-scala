package org.jetbrains.plugins.scala
package codeInsight
package intention
package types

import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.TemplateManagerImpl
import com.intellij.testFramework.TestModeFlags
import org.jetbrains.plugins.scala.codeInsight.intentions.ScalaIntentionTestBase
import org.jetbrains.plugins.scala.extensions.{executeUndoTransparentAction, executeWriteActionCommand}
import org.jetbrains.plugins.scala.project.settings.ScalaCompilerConfiguration
import org.junit.Assert.{assertNotNull, assertTrue, fail}

class ToggleTypeAnnotationIntentionTest extends ScalaIntentionTestBase {
  override protected def supportedIn(version: ScalaVersion): Boolean = version >= LatestScalaVersions.Scala_2_12

  override def familyName: String = ToggleTypeAnnotation.FamilyName

  override def setUp(): Unit = {
    super.setUp()
    val defaultProfile = ScalaCompilerConfiguration.instanceIn(getProject).defaultProfile
    val newSettings = defaultProfile.getSettings.copy(
      plugins = defaultProfile.getSettings.plugins :+ "kind-projector"
    )
    defaultProfile.setSettings(newSettings)
  }

  def testCollectionFactorySimplification(): Unit = doTest(
    "val v = Seq.empty[String]",
    "val v: Seq[String] = Seq.empty"
  )

  //for example for `val myValue = List.empty[String]` two options are shown: Seq[String] and List[String]
  def testCollectionFactorySimplification_MoreThenSingleValidTypeAnnotationCandidate(): Unit = {
    TemplateManagerImpl.setTemplateTesting(getTestRootDisposable)

    val text = "val v = List.empty[String]"
    myFixture.configureByText(fileType, text)
    val intention = findIntentionByName(familyName).getOrElse {
      fail("Intention is not found").asInstanceOf[Nothing]
    }

    executeWriteActionCommand("Test Intention Command") ({
      intention.invoke(getProject, getEditor, getFile)
    })(getProject)

    val templateManager = TemplateManager.getInstance(getProject)
    val activeTemplate = templateManager.getActiveTemplate(getEditor)
    assertNotNull("Expected to find some active template but found none", activeTemplate)

    val success = templateManager.finishTemplate(getEditor)
    assertTrue("Expected template to finish", success)

    val expectedResultText = "val v: Seq[String] = List.empty"
    myFixture.checkResult(expectedResultText)
  }

  def testCollectionFactoryNoSimplification(): Unit = doTest(
    "val v = Seq.empty[String].to[Seq]",
    "val v: Seq[String] = Seq.empty[String].to[Seq]"
  )

  def testOptionFactorySimplification(): Unit = doTest(
    "val v = Option.empty[String]",
    "val v: Option[String] = Option.empty"
  )

  def testOptionFactoryNoSimplification(): Unit = doTest(
    "val v = Option.empty[String].to[Option]",
    "val v: Option[String] = Option.empty[String].to[Option]"
  )

  def testCompoundType(): Unit = doTest(
    """val foo = new Runnable {
      |  def helper(): Unit = ???
      |
      |  override def run(): Unit = ???
      |}""".stripMargin,
    """val foo: Runnable = new Runnable {
      |  def helper(): Unit = ???
      |
      |  override def run(): Unit = ???
      |}""".stripMargin
  )

  def testCompoundTypeWithTypeMember(): Unit = doTest(
    s"""
       |trait Foo {
       |  type X
       |}
       |
       |val f${caretTag}oo = new Foo {
       |  override type X = Int
       |
       |  def helper(x: X): Unit = ???
       |}
     """.stripMargin,
    s"""
       |trait Foo {
       |  type X
       |}
       |
       |val f${caretTag}oo: Foo {
       |  type X = Int
       |} = new Foo {
       |  override type X = Int
       |
       |  def helper(x: X): Unit = ???
       |}
     """.stripMargin
  )

  def testInfixType(): Unit = doTest(
    s"""
       |trait A
       |
       |trait B
       |
       |def foo(): =:=[A, <:<[B, =:=[=:=[B, B], A]]] = ???
       |val ba${caretTag}r = foo()
     """.stripMargin,
    s"""
       |trait A
       |
       |trait B
       |
       |def foo(): =:=[A, <:<[B, =:=[=:=[B, B], A]]] = ???
       |val ba${caretTag}r: A =:= (B <:< (B =:= B =:= A)) = foo()
     """.stripMargin
  )

  def testInfixDifferentAssociativity(): Unit = doTest(
    s"""
       |trait +[A, B]
       |
       |trait ::[A, B]
       |
       |trait A
       |
       |def foo(): ::[+[A, +[::[A, A], A]], +[A, ::[A, A]]] = ???
       |val ba${caretTag}r = foo()
     """.stripMargin,
    s"""
       |trait +[A, B]
       |
       |trait ::[A, B]
       |
       |trait A
       |
       |def foo(): ::[+[A, +[::[A, A], A]], +[A, ::[A, A]]] = ???
       |val ba${caretTag}r: (A + ((A :: A) + A)) :: (A + (A :: A)) = foo()
     """.stripMargin
  )

  def testShowAsInfixAnnotation(): Unit = doTest(
    s"""
       |import scala.annotation.showAsInfix
       |
       |@showAsInfix class Map[A, B]
       |
       |def foo(): Map[Int, Map[Int, String]] = ???
       |val b${caretTag}ar = foo()
     """.stripMargin,
    s"""
       |import scala.annotation.showAsInfix
       |
       |@showAsInfix class Map[A, B]
       |
       |def foo(): Map[Int, Map[Int, String]] = ???
       |val b${caretTag}ar: Int Map (Int Map String) = foo()
     """.stripMargin
  )

  def testTupledFunction(): Unit = doTest(
    s"""class Test {
       |  def g(f: (String, Int) => Unit): Unit = {
       |    val ${caretTag}t = f.tupled // Add type annotation to value definition
       |  }
       |}""".stripMargin,
    s"""class Test {
       |  def g(f: (String, Int) => Unit): Unit = {
       |    val ${caretTag}t: ((String, Int)) => Unit = f.tupled // Add type annotation to value definition
       |  }
       |}""".stripMargin
  )

  def testTypeLambdaInline(): Unit = doTest(
    s"""
       |def foo: ({type L[A] = Either[String, A]})#L
       |val ${caretTag}v = foo
     """.stripMargin,
    s"""
       |def foo: ({type L[A] = Either[String, A]})#L
       |val ${caretTag}v: Either[String, ?] = foo
     """.stripMargin
  )

  def testTypeLambda(): Unit = doTest(
    s"""
       |def foo: ({type L[F[_]] = F[Int]})#L
       |val ${caretTag}v = foo
     """.stripMargin,
    s"""
       |def foo: ({type L[F[_]] = F[Int]})#L
       |val ${caretTag}v: Lambda[F[_] => F[Int]] = foo
     """.stripMargin
  )

  // see SCL-16739
  def testParameterAtEnd(): Unit = doTest(
    s"""
       |class Seq[T] {
       |  def foreach(f: T => Unit): Unit = ()
       |}
       |
       |val strings: Seq[String] = new Seq
       |strings.foreach(abc$caretTag => println(abc))
       |""".stripMargin,
    s"""
       |class Seq[T] {
       |  def foreach(f: T => Unit): Unit = ()
       |}
       |
       |val strings: Seq[String] = new Seq
       |strings.foreach((abc: String)$caretTag => println(abc))
       |""".stripMargin
  )
}
