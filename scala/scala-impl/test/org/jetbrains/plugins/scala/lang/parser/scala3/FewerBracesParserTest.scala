package org.jetbrains.plugins.scala.lang.parser.scala3

class FewerBracesParserTest extends SimpleScala3ParserTestBase {

  // ------------------------------------------------------
  //region Positive fewer braces parsing tests
  def test_fewer_braces(): Unit = checkTree(
    """val ys = xs.map: x =>
      |  x + 1
      |""".stripMargin,
    """
      |ScalaFile
      |  ScPatternDefinition: ys
      |    AnnotationsList
      |      <empty list>
      |    Modifiers
      |      <empty list>
      |    PsiElement(val)('val')
      |    PsiWhiteSpace(' ')
      |    ListOfPatterns
      |      ReferencePattern: ys
      |        PsiElement(identifier)('ys')
      |    PsiWhiteSpace(' ')
      |    PsiElement(=)('=')
      |    PsiWhiteSpace(' ')
      |    MethodCall
      |      ReferenceExpression: xs.map
      |        ReferenceExpression: xs
      |          PsiElement(identifier)('xs')
      |        PsiElement(.)('.')
      |        PsiElement(identifier)('map')
      |      ArgumentList
      |        BlockExpression
      |          PsiElement(:)(':')
      |          PsiWhiteSpace(' ')
      |          FunctionExpression
      |            Parameters
      |              ParametersClause
      |                Parameter: x
      |                  PsiElement(identifier)('x')
      |            PsiWhiteSpace(' ')
      |            PsiElement(=>)('=>')
      |            PsiWhiteSpace('\n  ')
      |            InfixExpression
      |              ReferenceExpression: x
      |                PsiElement(identifier)('x')
      |              PsiWhiteSpace(' ')
      |              ReferenceExpression: +
      |                PsiElement(identifier)('+')
      |              PsiWhiteSpace(' ')
      |              IntegerLiteral
      |                PsiElement(integer)('1')
      |  PsiWhiteSpace('\n')
      |""".stripMargin
  )

  def test_fewer_braces2(): Unit = checkTree(
    """val e = xs
      |  .filter: x =>
      |    x > 0
      |""".stripMargin,
    """
      |ScalaFile
      |  ScPatternDefinition: e
      |    AnnotationsList
      |      <empty list>
      |    Modifiers
      |      <empty list>
      |    PsiElement(val)('val')
      |    PsiWhiteSpace(' ')
      |    ListOfPatterns
      |      ReferencePattern: e
      |        PsiElement(identifier)('e')
      |    PsiWhiteSpace(' ')
      |    PsiElement(=)('=')
      |    PsiWhiteSpace(' ')
      |    MethodCall
      |      ReferenceExpression: xs
      |  .filter
      |        ReferenceExpression: xs
      |          PsiElement(identifier)('xs')
      |        PsiWhiteSpace('\n  ')
      |        PsiElement(.)('.')
      |        PsiElement(identifier)('filter')
      |      ArgumentList
      |        BlockExpression
      |          PsiElement(:)(':')
      |          PsiWhiteSpace(' ')
      |          FunctionExpression
      |            Parameters
      |              ParametersClause
      |                Parameter: x
      |                  PsiElement(identifier)('x')
      |            PsiWhiteSpace(' ')
      |            PsiElement(=>)('=>')
      |            PsiWhiteSpace('\n    ')
      |            InfixExpression
      |              ReferenceExpression: x
      |                PsiElement(identifier)('x')
      |              PsiWhiteSpace(' ')
      |              ReferenceExpression: >
      |                PsiElement(identifier)('>')
      |              PsiWhiteSpace(' ')
      |              IntegerLiteral
      |                PsiElement(integer)('0')
      |  PsiWhiteSpace('\n')
      |""".stripMargin
  )

  def test_fewer_braces_argument_on_new_line(): Unit = checkTree(
    """val e = xs
      |.filter:
      |  someFn
      |""".stripMargin,
    """
      |ScalaFile
      |  ScPatternDefinition: e
      |    AnnotationsList
      |      <empty list>
      |    Modifiers
      |      <empty list>
      |    PsiElement(val)('val')
      |    PsiWhiteSpace(' ')
      |    ListOfPatterns
      |      ReferencePattern: e
      |        PsiElement(identifier)('e')
      |    PsiWhiteSpace(' ')
      |    PsiElement(=)('=')
      |    PsiWhiteSpace(' ')
      |    MethodCall
      |      ReferenceExpression: xs
      |.filter
      |        ReferenceExpression: xs
      |          PsiElement(identifier)('xs')
      |        PsiWhiteSpace('\n')
      |        PsiElement(.)('.')
      |        PsiElement(identifier)('filter')
      |      ArgumentList
      |        BlockExpression
      |          PsiElement(:)(':')
      |          PsiWhiteSpace('\n  ')
      |          ReferenceExpression: someFn
      |            PsiElement(identifier)('someFn')
      |  PsiWhiteSpace('\n')
      |""".stripMargin
  )

  def test_fewer_braces_implicit_on_new_line(): Unit = checkTree(
    """val e = xs
      |  .filter:
      |    implicit x => x > 0
      |""".stripMargin,
    """
      |ScalaFile
      |  ScPatternDefinition: e
      |    AnnotationsList
      |      <empty list>
      |    Modifiers
      |      <empty list>
      |    PsiElement(val)('val')
      |    PsiWhiteSpace(' ')
      |    ListOfPatterns
      |      ReferencePattern: e
      |        PsiElement(identifier)('e')
      |    PsiWhiteSpace(' ')
      |    PsiElement(=)('=')
      |    PsiWhiteSpace(' ')
      |    MethodCall
      |      ReferenceExpression: xs
      |  .filter
      |        ReferenceExpression: xs
      |          PsiElement(identifier)('xs')
      |        PsiWhiteSpace('\n  ')
      |        PsiElement(.)('.')
      |        PsiElement(identifier)('filter')
      |      ArgumentList
      |        BlockExpression
      |          PsiElement(:)(':')
      |          PsiWhiteSpace('\n    ')
      |          FunctionExpression
      |            Parameters
      |              ParametersClause
      |                PsiElement(implicit)('implicit')
      |                PsiWhiteSpace(' ')
      |                Parameter: x
      |                  PsiElement(identifier)('x')
      |            PsiWhiteSpace(' ')
      |            PsiElement(=>)('=>')
      |            PsiWhiteSpace(' ')
      |            BlockOfExpressions
      |              InfixExpression
      |                ReferenceExpression: x
      |                  PsiElement(identifier)('x')
      |                PsiWhiteSpace(' ')
      |                ReferenceExpression: >
      |                  PsiElement(identifier)('>')
      |                PsiWhiteSpace(' ')
      |                IntegerLiteral
      |                  PsiElement(integer)('0')
      |  PsiWhiteSpace('\n')
      |""".stripMargin
  )

  def test_fewer_braces_infix(): Unit = checkTree(
    """List(1) map: x =>
      |    x + 1
      |""".stripMargin,
    """
      |ScalaFile
      |  InfixExpression
      |    MethodCall
      |      ReferenceExpression: List
      |        PsiElement(identifier)('List')
      |      ArgumentList
      |        PsiElement(()('(')
      |        IntegerLiteral
      |          PsiElement(integer)('1')
      |        PsiElement())(')')
      |    PsiWhiteSpace(' ')
      |    ReferenceExpression: map
      |      PsiElement(identifier)('map')
      |    BlockExpression
      |      PsiElement(:)(':')
      |      PsiWhiteSpace(' ')
      |      FunctionExpression
      |        Parameters
      |          ParametersClause
      |            Parameter: x
      |              PsiElement(identifier)('x')
      |        PsiWhiteSpace(' ')
      |        PsiElement(=>)('=>')
      |        PsiWhiteSpace('\n    ')
      |        InfixExpression
      |          ReferenceExpression: x
      |            PsiElement(identifier)('x')
      |          PsiWhiteSpace(' ')
      |          ReferenceExpression: +
      |            PsiElement(identifier)('+')
      |          PsiWhiteSpace(' ')
      |          IntegerLiteral
      |            PsiElement(integer)('1')
      |  PsiWhiteSpace('\n')
      |""".stripMargin
  )

  def test_fewer_braces_bindings(): Unit = checkTree(
    """val x = ys.foldLeft(0): (x, y) =>
      |  x + y
      |""".stripMargin,
    """
      |ScalaFile
      |  ScPatternDefinition: x
      |    AnnotationsList
      |      <empty list>
      |    Modifiers
      |      <empty list>
      |    PsiElement(val)('val')
      |    PsiWhiteSpace(' ')
      |    ListOfPatterns
      |      ReferencePattern: x
      |        PsiElement(identifier)('x')
      |    PsiWhiteSpace(' ')
      |    PsiElement(=)('=')
      |    PsiWhiteSpace(' ')
      |    MethodCall
      |      MethodCall
      |        ReferenceExpression: ys.foldLeft
      |          ReferenceExpression: ys
      |            PsiElement(identifier)('ys')
      |          PsiElement(.)('.')
      |          PsiElement(identifier)('foldLeft')
      |        ArgumentList
      |          PsiElement(()('(')
      |          IntegerLiteral
      |            PsiElement(integer)('0')
      |          PsiElement())(')')
      |      ArgumentList
      |        BlockExpression
      |          PsiElement(:)(':')
      |          PsiWhiteSpace(' ')
      |          FunctionExpression
      |            Parameters
      |              ParametersClause
      |                PsiElement(()('(')
      |                Parameter: x
      |                  AnnotationsList
      |                    <empty list>
      |                  PsiElement(identifier)('x')
      |                PsiElement(,)(',')
      |                PsiWhiteSpace(' ')
      |                Parameter: y
      |                  AnnotationsList
      |                    <empty list>
      |                  PsiElement(identifier)('y')
      |                PsiElement())(')')
      |            PsiWhiteSpace(' ')
      |            PsiElement(=>)('=>')
      |            PsiWhiteSpace('\n  ')
      |            InfixExpression
      |              ReferenceExpression: x
      |                PsiElement(identifier)('x')
      |              PsiWhiteSpace(' ')
      |              ReferenceExpression: +
      |                PsiElement(identifier)('+')
      |              PsiWhiteSpace(' ')
      |              ReferenceExpression: y
      |                PsiElement(identifier)('y')
      |  PsiWhiteSpace('\n')
      |""".stripMargin
  )

  def test_fewer_braces_bindings_with_types(): Unit = checkTree(
    """val y = ys.foldLeft(0): (x: Int, y: Int) =>
      |  val z = x + y
      |  z * z
      |""".stripMargin,
    """
      |ScalaFile
      |  ScPatternDefinition: y
      |    AnnotationsList
      |      <empty list>
      |    Modifiers
      |      <empty list>
      |    PsiElement(val)('val')
      |    PsiWhiteSpace(' ')
      |    ListOfPatterns
      |      ReferencePattern: y
      |        PsiElement(identifier)('y')
      |    PsiWhiteSpace(' ')
      |    PsiElement(=)('=')
      |    PsiWhiteSpace(' ')
      |    MethodCall
      |      MethodCall
      |        ReferenceExpression: ys.foldLeft
      |          ReferenceExpression: ys
      |            PsiElement(identifier)('ys')
      |          PsiElement(.)('.')
      |          PsiElement(identifier)('foldLeft')
      |        ArgumentList
      |          PsiElement(()('(')
      |          IntegerLiteral
      |            PsiElement(integer)('0')
      |          PsiElement())(')')
      |      ArgumentList
      |        BlockExpression
      |          PsiElement(:)(':')
      |          PsiWhiteSpace(' ')
      |          FunctionExpression
      |            Parameters
      |              ParametersClause
      |                PsiElement(()('(')
      |                Parameter: x
      |                  AnnotationsList
      |                    <empty list>
      |                  PsiElement(identifier)('x')
      |                  PsiElement(:)(':')
      |                  PsiWhiteSpace(' ')
      |                  ParameterType
      |                    SimpleType: Int
      |                      CodeReferenceElement: Int
      |                        PsiElement(identifier)('Int')
      |                PsiElement(,)(',')
      |                PsiWhiteSpace(' ')
      |                Parameter: y
      |                  AnnotationsList
      |                    <empty list>
      |                  PsiElement(identifier)('y')
      |                  PsiElement(:)(':')
      |                  PsiWhiteSpace(' ')
      |                  ParameterType
      |                    SimpleType: Int
      |                      CodeReferenceElement: Int
      |                        PsiElement(identifier)('Int')
      |                PsiElement())(')')
      |            PsiWhiteSpace(' ')
      |            PsiElement(=>)('=>')
      |            PsiWhiteSpace('\n  ')
      |            BlockOfExpressions
      |              ScPatternDefinition: z
      |                AnnotationsList
      |                  <empty list>
      |                Modifiers
      |                  <empty list>
      |                PsiElement(val)('val')
      |                PsiWhiteSpace(' ')
      |                ListOfPatterns
      |                  ReferencePattern: z
      |                    PsiElement(identifier)('z')
      |                PsiWhiteSpace(' ')
      |                PsiElement(=)('=')
      |                PsiWhiteSpace(' ')
      |                InfixExpression
      |                  ReferenceExpression: x
      |                    PsiElement(identifier)('x')
      |                  PsiWhiteSpace(' ')
      |                  ReferenceExpression: +
      |                    PsiElement(identifier)('+')
      |                  PsiWhiteSpace(' ')
      |                  ReferenceExpression: y
      |                    PsiElement(identifier)('y')
      |              PsiWhiteSpace('\n  ')
      |              InfixExpression
      |                ReferenceExpression: z
      |                  PsiElement(identifier)('z')
      |                PsiWhiteSpace(' ')
      |                ReferenceExpression: *
      |                  PsiElement(identifier)('*')
      |                PsiWhiteSpace(' ')
      |                ReferenceExpression: z
      |                  PsiElement(identifier)('z')
      |  PsiWhiteSpace('\n')
      |""".stripMargin
  )

  def test_fewer_braces_simple_arg_in_parens(): Unit = checkTree(
    """val a = xs
      |  (0)
      |""".stripMargin,
    """
      |ScalaFile
      |  ScPatternDefinition: a
      |    AnnotationsList
      |      <empty list>
      |    Modifiers
      |      <empty list>
      |    PsiElement(val)('val')
      |    PsiWhiteSpace(' ')
      |    ListOfPatterns
      |      ReferencePattern: a
      |        PsiElement(identifier)('a')
      |    PsiWhiteSpace(' ')
      |    PsiElement(=)('=')
      |    PsiWhiteSpace(' ')
      |    MethodCall
      |      ReferenceExpression: xs
      |        PsiElement(identifier)('xs')
      |      PsiWhiteSpace('\n  ')
      |      ArgumentList
      |        PsiElement(()('(')
      |        IntegerLiteral
      |          PsiElement(integer)('0')
      |        PsiElement())(')')
      |  PsiWhiteSpace('\n')
      |""".stripMargin
  )

  def test_fewer_braces_simple_arg_in_parens2(): Unit = checkTree(
    """val a =
      |  xs
      |   (0)
      |""".stripMargin,
    """
      |ScalaFile
      |  ScPatternDefinition: a
      |    AnnotationsList
      |      <empty list>
      |    Modifiers
      |      <empty list>
      |    PsiElement(val)('val')
      |    PsiWhiteSpace(' ')
      |    ListOfPatterns
      |      ReferencePattern: a
      |        PsiElement(identifier)('a')
      |    PsiWhiteSpace(' ')
      |    PsiElement(=)('=')
      |    PsiWhiteSpace('\n  ')
      |    MethodCall
      |      ReferenceExpression: xs
      |        PsiElement(identifier)('xs')
      |      PsiWhiteSpace('\n   ')
      |      ArgumentList
      |        PsiElement(()('(')
      |        IntegerLiteral
      |          PsiElement(integer)('0')
      |        PsiElement())(')')
      |  PsiWhiteSpace('\n')
      |""".stripMargin
  )

  def test_fewer_braces_multiple_calls_and_args_in_parens(): Unit = checkTree(
    """val a: Int = xs
      |  .map: x =>
      |    x * x
      |  .filter: (y: Int) =>
      |    y > 0
      |  (0)
      |""".stripMargin,
    """
      |ScalaFile
      |  ScPatternDefinition: a
      |    AnnotationsList
      |      <empty list>
      |    Modifiers
      |      <empty list>
      |    PsiElement(val)('val')
      |    PsiWhiteSpace(' ')
      |    ListOfPatterns
      |      ReferencePattern: a
      |        PsiElement(identifier)('a')
      |    PsiElement(:)(':')
      |    PsiWhiteSpace(' ')
      |    SimpleType: Int
      |      CodeReferenceElement: Int
      |        PsiElement(identifier)('Int')
      |    PsiWhiteSpace(' ')
      |    PsiElement(=)('=')
      |    PsiWhiteSpace(' ')
      |    MethodCall
      |      MethodCall
      |        ReferenceExpression: xs
      |  .map: x =>
      |    x * x
      |  .filter
      |          MethodCall
      |            ReferenceExpression: xs
      |  .map
      |              ReferenceExpression: xs
      |                PsiElement(identifier)('xs')
      |              PsiWhiteSpace('\n  ')
      |              PsiElement(.)('.')
      |              PsiElement(identifier)('map')
      |            ArgumentList
      |              BlockExpression
      |                PsiElement(:)(':')
      |                PsiWhiteSpace(' ')
      |                FunctionExpression
      |                  Parameters
      |                    ParametersClause
      |                      Parameter: x
      |                        PsiElement(identifier)('x')
      |                  PsiWhiteSpace(' ')
      |                  PsiElement(=>)('=>')
      |                  PsiWhiteSpace('\n    ')
      |                  InfixExpression
      |                    ReferenceExpression: x
      |                      PsiElement(identifier)('x')
      |                    PsiWhiteSpace(' ')
      |                    ReferenceExpression: *
      |                      PsiElement(identifier)('*')
      |                    PsiWhiteSpace(' ')
      |                    ReferenceExpression: x
      |                      PsiElement(identifier)('x')
      |          PsiWhiteSpace('\n  ')
      |          PsiElement(.)('.')
      |          PsiElement(identifier)('filter')
      |        ArgumentList
      |          BlockExpression
      |            PsiElement(:)(':')
      |            PsiWhiteSpace(' ')
      |            FunctionExpression
      |              Parameters
      |                ParametersClause
      |                  PsiElement(()('(')
      |                  Parameter: y
      |                    AnnotationsList
      |                      <empty list>
      |                    PsiElement(identifier)('y')
      |                    PsiElement(:)(':')
      |                    PsiWhiteSpace(' ')
      |                    ParameterType
      |                      SimpleType: Int
      |                        CodeReferenceElement: Int
      |                          PsiElement(identifier)('Int')
      |                  PsiElement())(')')
      |              PsiWhiteSpace(' ')
      |              PsiElement(=>)('=>')
      |              PsiWhiteSpace('\n    ')
      |              InfixExpression
      |                ReferenceExpression: y
      |                  PsiElement(identifier)('y')
      |                PsiWhiteSpace(' ')
      |                ReferenceExpression: >
      |                  PsiElement(identifier)('>')
      |                PsiWhiteSpace(' ')
      |                IntegerLiteral
      |                  PsiElement(integer)('0')
      |      PsiWhiteSpace('\n  ')
      |      ArgumentList
      |        PsiElement(()('(')
      |        IntegerLiteral
      |          PsiElement(integer)('0')
      |        PsiElement())(')')
      |  PsiWhiteSpace('\n')
      |""".stripMargin
  )

  def test_fewer_braces_case_clauses(): Unit = checkTree(
    """val e = xs.map:
      |    case 1 => 2
      |    case x => x
      |  .filter: x =>
      |    x > 0
      |""".stripMargin,
    """
      |ScalaFile
      |  ScPatternDefinition: e
      |    AnnotationsList
      |      <empty list>
      |    Modifiers
      |      <empty list>
      |    PsiElement(val)('val')
      |    PsiWhiteSpace(' ')
      |    ListOfPatterns
      |      ReferencePattern: e
      |        PsiElement(identifier)('e')
      |    PsiWhiteSpace(' ')
      |    PsiElement(=)('=')
      |    PsiWhiteSpace(' ')
      |    MethodCall
      |      ReferenceExpression: xs.map:
      |    case 1 => 2
      |    case x => x
      |  .filter
      |        MethodCall
      |          ReferenceExpression: xs.map
      |            ReferenceExpression: xs
      |              PsiElement(identifier)('xs')
      |            PsiElement(.)('.')
      |            PsiElement(identifier)('map')
      |          ArgumentList
      |            BlockExpression
      |              PsiElement(:)(':')
      |              PsiWhiteSpace('\n    ')
      |              CaseClauses
      |                CaseClause
      |                  PsiElement(case)('case')
      |                  PsiWhiteSpace(' ')
      |                  LiteralPattern
      |                    IntegerLiteral
      |                      PsiElement(integer)('1')
      |                  PsiWhiteSpace(' ')
      |                  PsiElement(=>)('=>')
      |                  PsiWhiteSpace(' ')
      |                  BlockOfExpressions
      |                    IntegerLiteral
      |                      PsiElement(integer)('2')
      |                PsiWhiteSpace('\n    ')
      |                CaseClause
      |                  PsiElement(case)('case')
      |                  PsiWhiteSpace(' ')
      |                  ReferencePattern: x
      |                    PsiElement(identifier)('x')
      |                  PsiWhiteSpace(' ')
      |                  PsiElement(=>)('=>')
      |                  PsiWhiteSpace(' ')
      |                  BlockOfExpressions
      |                    ReferenceExpression: x
      |                      PsiElement(identifier)('x')
      |        PsiWhiteSpace('\n  ')
      |        PsiElement(.)('.')
      |        PsiElement(identifier)('filter')
      |      ArgumentList
      |        BlockExpression
      |          PsiElement(:)(':')
      |          PsiWhiteSpace(' ')
      |          FunctionExpression
      |            Parameters
      |              ParametersClause
      |                Parameter: x
      |                  PsiElement(identifier)('x')
      |            PsiWhiteSpace(' ')
      |            PsiElement(=>)('=>')
      |            PsiWhiteSpace('\n    ')
      |            InfixExpression
      |              ReferenceExpression: x
      |                PsiElement(identifier)('x')
      |              PsiWhiteSpace(' ')
      |              ReferenceExpression: >
      |                PsiElement(identifier)('>')
      |              PsiWhiteSpace(' ')
      |              IntegerLiteral
      |                PsiElement(integer)('0')
      |  PsiWhiteSpace('\n')
      |""".stripMargin
  )

  def test_fewer_braces_poly_function(): Unit = checkTree(
    """val p = xs.foo:
      |  [X] => (x: X) => x
      |""".stripMargin,
    """
      |ScalaFile
      |  ScPatternDefinition: p
      |    AnnotationsList
      |      <empty list>
      |    Modifiers
      |      <empty list>
      |    PsiElement(val)('val')
      |    PsiWhiteSpace(' ')
      |    ListOfPatterns
      |      ReferencePattern: p
      |        PsiElement(identifier)('p')
      |    PsiWhiteSpace(' ')
      |    PsiElement(=)('=')
      |    PsiWhiteSpace(' ')
      |    MethodCall
      |      ReferenceExpression: xs.foo
      |        ReferenceExpression: xs
      |          PsiElement(identifier)('xs')
      |        PsiElement(.)('.')
      |        PsiElement(identifier)('foo')
      |      ArgumentList
      |        BlockExpression
      |          PsiElement(:)(':')
      |          PsiWhiteSpace('\n  ')
      |          PolyFunctionExpression
      |            TypeParameterClause
      |              PsiElement([)('[')
      |              TypeParameter: X
      |                PsiElement(identifier)('X')
      |              PsiElement(])(']')
      |            PsiWhiteSpace(' ')
      |            PsiElement(=>)('=>')
      |            PsiWhiteSpace(' ')
      |            FunctionExpression
      |              Parameters
      |                ParametersClause
      |                  PsiElement(()('(')
      |                  Parameter: x
      |                    AnnotationsList
      |                      <empty list>
      |                    PsiElement(identifier)('x')
      |                    PsiElement(:)(':')
      |                    PsiWhiteSpace(' ')
      |                    ParameterType
      |                      SimpleType: X
      |                        CodeReferenceElement: X
      |                          PsiElement(identifier)('X')
      |                  PsiElement())(')')
      |              PsiWhiteSpace(' ')
      |              PsiElement(=>)('=>')
      |              PsiWhiteSpace(' ')
      |              BlockOfExpressions
      |                ReferenceExpression: x
      |                  PsiElement(identifier)('x')
      |  PsiWhiteSpace('\n')
      |""".stripMargin
  )

  def test_fewer_braces_poly_function2(): Unit = checkTree(
    """val p = xs.foo: [X] =>
      |  (x: X) => x
      |""".stripMargin,
    """
      |ScalaFile
      |  ScPatternDefinition: p
      |    AnnotationsList
      |      <empty list>
      |    Modifiers
      |      <empty list>
      |    PsiElement(val)('val')
      |    PsiWhiteSpace(' ')
      |    ListOfPatterns
      |      ReferencePattern: p
      |        PsiElement(identifier)('p')
      |    PsiWhiteSpace(' ')
      |    PsiElement(=)('=')
      |    PsiWhiteSpace(' ')
      |    MethodCall
      |      ReferenceExpression: xs.foo
      |        ReferenceExpression: xs
      |          PsiElement(identifier)('xs')
      |        PsiElement(.)('.')
      |        PsiElement(identifier)('foo')
      |      ArgumentList
      |        BlockExpression
      |          PsiElement(:)(':')
      |          PsiWhiteSpace(' ')
      |          PolyFunctionExpression
      |            TypeParameterClause
      |              PsiElement([)('[')
      |              TypeParameter: X
      |                PsiElement(identifier)('X')
      |              PsiElement(])(']')
      |            PsiWhiteSpace(' ')
      |            PsiElement(=>)('=>')
      |            PsiWhiteSpace('\n  ')
      |            FunctionExpression
      |              Parameters
      |                ParametersClause
      |                  PsiElement(()('(')
      |                  Parameter: x
      |                    AnnotationsList
      |                      <empty list>
      |                    PsiElement(identifier)('x')
      |                    PsiElement(:)(':')
      |                    PsiWhiteSpace(' ')
      |                    ParameterType
      |                      SimpleType: X
      |                        CodeReferenceElement: X
      |                          PsiElement(identifier)('X')
      |                  PsiElement())(')')
      |              PsiWhiteSpace(' ')
      |              PsiElement(=>)('=>')
      |              PsiWhiteSpace(' ')
      |              BlockOfExpressions
      |                ReferenceExpression: x
      |                  PsiElement(identifier)('x')
      |  PsiWhiteSpace('\n')
      |""".stripMargin
  )

  def test_fewer_braces_simple_call(): Unit = checkTree(
    """locally:
      |  y > 0
      |""".stripMargin,
    """
      |ScalaFile
      |  MethodCall
      |    ReferenceExpression: locally
      |      PsiElement(identifier)('locally')
      |    ArgumentList
      |      BlockExpression
      |        PsiElement(:)(':')
      |        PsiWhiteSpace('\n  ')
      |        InfixExpression
      |          ReferenceExpression: y
      |            PsiElement(identifier)('y')
      |          PsiWhiteSpace(' ')
      |          ReferenceExpression: >
      |            PsiElement(identifier)('>')
      |          PsiWhiteSpace(' ')
      |          IntegerLiteral
      |            PsiElement(integer)('0')
      |  PsiWhiteSpace('\n')
      |""".stripMargin
  )

  def test_fewer_braces_call_in_infix_expr(): Unit = checkTree(
    """val r = x < 0 && locally:
      |  y > 0
      |""".stripMargin,
    """
      |ScalaFile
      |  ScPatternDefinition: r
      |    AnnotationsList
      |      <empty list>
      |    Modifiers
      |      <empty list>
      |    PsiElement(val)('val')
      |    PsiWhiteSpace(' ')
      |    ListOfPatterns
      |      ReferencePattern: r
      |        PsiElement(identifier)('r')
      |    PsiWhiteSpace(' ')
      |    PsiElement(=)('=')
      |    PsiWhiteSpace(' ')
      |    InfixExpression
      |      InfixExpression
      |        ReferenceExpression: x
      |          PsiElement(identifier)('x')
      |        PsiWhiteSpace(' ')
      |        ReferenceExpression: <
      |          PsiElement(identifier)('<')
      |        PsiWhiteSpace(' ')
      |        IntegerLiteral
      |          PsiElement(integer)('0')
      |      PsiWhiteSpace(' ')
      |      ReferenceExpression: &&
      |        PsiElement(identifier)('&&')
      |      PsiWhiteSpace(' ')
      |      MethodCall
      |        ReferenceExpression: locally
      |          PsiElement(identifier)('locally')
      |        ArgumentList
      |          BlockExpression
      |            PsiElement(:)(':')
      |            PsiWhiteSpace('\n  ')
      |            InfixExpression
      |              ReferenceExpression: y
      |                PsiElement(identifier)('y')
      |              PsiWhiteSpace(' ')
      |              ReferenceExpression: >
      |                PsiElement(identifier)('>')
      |              PsiWhiteSpace(' ')
      |              IntegerLiteral
      |                PsiElement(integer)('0')
      |  PsiWhiteSpace('\n')
      |""".stripMargin
  )

  def test_fewer_braces_multiple_exprs_block_closes_on_outdent(): Unit = checkTree(
    """object Test:
      |  xo.fold:
      |    2
      |  .apply: x =>
      |    x + 2
      |
      |  xo.map: x =>
      |    x - 2
      |""".stripMargin,
    """
      |ScalaFile
      |  ScObject: Test
      |    AnnotationsList
      |      <empty list>
      |    Modifiers
      |      <empty list>
      |    PsiElement(object)('object')
      |    PsiWhiteSpace(' ')
      |    PsiElement(identifier)('Test')
      |    ExtendsBlock
      |      ScTemplateBody
      |        PsiElement(:)(':')
      |        PsiWhiteSpace('\n  ')
      |        MethodCall
      |          ReferenceExpression: xo.fold:
      |    2
      |  .apply
      |            MethodCall
      |              ReferenceExpression: xo.fold
      |                ReferenceExpression: xo
      |                  PsiElement(identifier)('xo')
      |                PsiElement(.)('.')
      |                PsiElement(identifier)('fold')
      |              ArgumentList
      |                BlockExpression
      |                  PsiElement(:)(':')
      |                  PsiWhiteSpace('\n    ')
      |                  IntegerLiteral
      |                    PsiElement(integer)('2')
      |            PsiWhiteSpace('\n  ')
      |            PsiElement(.)('.')
      |            PsiElement(identifier)('apply')
      |          ArgumentList
      |            BlockExpression
      |              PsiElement(:)(':')
      |              PsiWhiteSpace(' ')
      |              FunctionExpression
      |                Parameters
      |                  ParametersClause
      |                    Parameter: x
      |                      PsiElement(identifier)('x')
      |                PsiWhiteSpace(' ')
      |                PsiElement(=>)('=>')
      |                PsiWhiteSpace('\n    ')
      |                InfixExpression
      |                  ReferenceExpression: x
      |                    PsiElement(identifier)('x')
      |                  PsiWhiteSpace(' ')
      |                  ReferenceExpression: +
      |                    PsiElement(identifier)('+')
      |                  PsiWhiteSpace(' ')
      |                  IntegerLiteral
      |                    PsiElement(integer)('2')
      |        PsiWhiteSpace('\n\n  ')
      |        MethodCall
      |          ReferenceExpression: xo.map
      |            ReferenceExpression: xo
      |              PsiElement(identifier)('xo')
      |            PsiElement(.)('.')
      |            PsiElement(identifier)('map')
      |          ArgumentList
      |            BlockExpression
      |              PsiElement(:)(':')
      |              PsiWhiteSpace(' ')
      |              FunctionExpression
      |                Parameters
      |                  ParametersClause
      |                    Parameter: x
      |                      PsiElement(identifier)('x')
      |                PsiWhiteSpace(' ')
      |                PsiElement(=>)('=>')
      |                PsiWhiteSpace('\n    ')
      |                InfixExpression
      |                  ReferenceExpression: x
      |                    PsiElement(identifier)('x')
      |                  PsiWhiteSpace(' ')
      |                  ReferenceExpression: -
      |                    PsiElement(identifier)('-')
      |                  PsiWhiteSpace(' ')
      |                  IntegerLiteral
      |                    PsiElement(integer)('2')
      |  PsiWhiteSpace('\n')
      |""".stripMargin
  )
  //endregion
  // ------------------------------------------------------

  // ------------------------------------------------------
  //region Negative fewer braces parsing tests
  def test_fewer_braces_one_line_lambda__negative(): Unit = checkTree(
    """val e = xs
      |.filter: x => x > 0
      |""".stripMargin,
    """
      |ScalaFile
      |  ScPatternDefinition: e
      |    AnnotationsList
      |      <empty list>
      |    Modifiers
      |      <empty list>
      |    PsiElement(val)('val')
      |    PsiWhiteSpace(' ')
      |    ListOfPatterns
      |      ReferencePattern: e
      |        PsiElement(identifier)('e')
      |    PsiWhiteSpace(' ')
      |    PsiElement(=)('=')
      |    PsiWhiteSpace(' ')
      |    TypedExpression
      |      ReferenceExpression: xs
      |.filter
      |        ReferenceExpression: xs
      |          PsiElement(identifier)('xs')
      |        PsiWhiteSpace('\n')
      |        PsiElement(.)('.')
      |        PsiElement(identifier)('filter')
      |      PsiElement(:)(':')
      |      PsiWhiteSpace(' ')
      |      FunctionalType: x => x > 0
      |        SimpleType: x
      |          CodeReferenceElement: x
      |            PsiElement(identifier)('x')
      |        PsiWhiteSpace(' ')
      |        PsiElement(=>)('=>')
      |        PsiWhiteSpace(' ')
      |        InfixType: x > 0
      |          SimpleType: x
      |            CodeReferenceElement: x
      |              PsiElement(identifier)('x')
      |          PsiWhiteSpace(' ')
      |          CodeReferenceElement: >
      |            PsiElement(identifier)('>')
      |          PsiWhiteSpace(' ')
      |          LiteralType: 0
      |            IntegerLiteral
      |              PsiElement(integer)('0')
      |  PsiWhiteSpace('\n')
      |""".stripMargin
  )

  def test_fewer_braces_argument_after_colon__negative(): Unit = checkTree(
    """val e = xs
      |.filter: someFn
      |""".stripMargin,
    """
      |ScalaFile
      |  ScPatternDefinition: e
      |    AnnotationsList
      |      <empty list>
      |    Modifiers
      |      <empty list>
      |    PsiElement(val)('val')
      |    PsiWhiteSpace(' ')
      |    ListOfPatterns
      |      ReferencePattern: e
      |        PsiElement(identifier)('e')
      |    PsiWhiteSpace(' ')
      |    PsiElement(=)('=')
      |    PsiWhiteSpace(' ')
      |    TypedExpression
      |      ReferenceExpression: xs
      |.filter
      |        ReferenceExpression: xs
      |          PsiElement(identifier)('xs')
      |        PsiWhiteSpace('\n')
      |        PsiElement(.)('.')
      |        PsiElement(identifier)('filter')
      |      PsiElement(:)(':')
      |      PsiWhiteSpace(' ')
      |      SimpleType: someFn
      |        CodeReferenceElement: someFn
      |          PsiElement(identifier)('someFn')
      |  PsiWhiteSpace('\n')
      |""".stripMargin
  )

  def test_fewer_braces_implicit__negative(): Unit = checkTree(
    """val e = xs
      |  .filter: implicit x =>
      |    x > 0
      |""".stripMargin,
    """
      |ScalaFile
      |  ScPatternDefinition: e
      |    AnnotationsList
      |      <empty list>
      |    Modifiers
      |      <empty list>
      |    PsiElement(val)('val')
      |    PsiWhiteSpace(' ')
      |    ListOfPatterns
      |      ReferencePattern: e
      |        PsiElement(identifier)('e')
      |    PsiWhiteSpace(' ')
      |    PsiElement(=)('=')
      |    PsiWhiteSpace(' ')
      |    TypedExpression
      |      ReferenceExpression: xs
      |  .filter
      |        ReferenceExpression: xs
      |          PsiElement(identifier)('xs')
      |        PsiWhiteSpace('\n  ')
      |        PsiElement(.)('.')
      |        PsiElement(identifier)('filter')
      |      PsiElement(:)(':')
      |      AnnotationsList
      |        <empty list>
      |      PsiErrorElement:Annotation or type expected
      |        <empty list>
      |  PsiWhiteSpace(' ')
      |  FunctionExpression
      |    Parameters
      |      ParametersClause
      |        PsiElement(implicit)('implicit')
      |        PsiWhiteSpace(' ')
      |        Parameter: x
      |          PsiElement(identifier)('x')
      |    PsiWhiteSpace(' ')
      |    PsiElement(=>)('=>')
      |    PsiWhiteSpace('\n    ')
      |    InfixExpression
      |      ReferenceExpression: x
      |        PsiElement(identifier)('x')
      |      PsiWhiteSpace(' ')
      |      ReferenceExpression: >
      |        PsiElement(identifier)('>')
      |      PsiWhiteSpace(' ')
      |      IntegerLiteral
      |        PsiElement(integer)('0')
      |  PsiWhiteSpace('\n')
      |""".stripMargin
  )

  def test_fewer_braces_one_line_case_clause__negative(): Unit = checkTree(
    """xs.map: case x => x + 1
      |""".stripMargin,
    """
      |ScalaFile
      |  TypedExpression
      |    ReferenceExpression: xs.map
      |      ReferenceExpression: xs
      |        PsiElement(identifier)('xs')
      |      PsiElement(.)('.')
      |      PsiElement(identifier)('map')
      |    PsiElement(:)(':')
      |    AnnotationsList
      |      <empty list>
      |    PsiErrorElement:Annotation or type expected
      |      <empty list>
      |  PsiWhiteSpace(' ')
      |  PsiElement(case)('case')
      |  PsiWhiteSpace(' ')
      |  FunctionExpression
      |    Parameters
      |      ParametersClause
      |        Parameter: x
      |          PsiElement(identifier)('x')
      |    PsiWhiteSpace(' ')
      |    PsiElement(=>)('=>')
      |    PsiWhiteSpace(' ')
      |    InfixExpression
      |      ReferenceExpression: x
      |        PsiElement(identifier)('x')
      |      PsiWhiteSpace(' ')
      |      ReferenceExpression: +
      |        PsiElement(identifier)('+')
      |      PsiWhiteSpace(' ')
      |      IntegerLiteral
      |        PsiElement(integer)('1')
      |  PsiWhiteSpace('\n')
      |""".stripMargin
  )

  def test_fewer_braces_poly_function_one_line__negative(): Unit = checkTree(
    """val p = xs.foo: [X] => (x: X) => x
      |""".stripMargin,
    """
      |ScalaFile
      |  ScPatternDefinition: p
      |    AnnotationsList
      |      <empty list>
      |    Modifiers
      |      <empty list>
      |    PsiElement(val)('val')
      |    PsiWhiteSpace(' ')
      |    ListOfPatterns
      |      ReferencePattern: p
      |        PsiElement(identifier)('p')
      |    PsiWhiteSpace(' ')
      |    PsiElement(=)('=')
      |    PsiWhiteSpace(' ')
      |    TypedExpression
      |      ReferenceExpression: xs.foo
      |        ReferenceExpression: xs
      |          PsiElement(identifier)('xs')
      |        PsiElement(.)('.')
      |        PsiElement(identifier)('foo')
      |      PsiElement(:)(':')
      |      PsiWhiteSpace(' ')
      |      PolymorhicFunctionType: [X] => (x: X) => x
      |        TypeParameterClause
      |          PsiElement([)('[')
      |          TypeParameter: X
      |            PsiElement(identifier)('X')
      |          PsiElement(])(']')
      |        PsiWhiteSpace(' ')
      |        PsiElement(=>)('=>')
      |        PsiWhiteSpace(' ')
      |        DependentFunctionType: (x: X) => x
      |          ParametersClause
      |            PsiElement(()('(')
      |            Parameter: x
      |              PsiElement(identifier)('x')
      |              PsiElement(:)(':')
      |              PsiWhiteSpace(' ')
      |              SimpleType: X
      |                CodeReferenceElement: X
      |                  PsiElement(identifier)('X')
      |            PsiElement())(')')
      |          PsiWhiteSpace(' ')
      |          PsiElement(=>)('=>')
      |          PsiWhiteSpace(' ')
      |          SimpleType: x
      |            CodeReferenceElement: x
      |              PsiElement(identifier)('x')
      |  PsiWhiteSpace('\n')
      |""".stripMargin
  )
  //endregion
  // ------------------------------------------------------

  // ------------------------------------------------------
  //region Sanity check
  def test_fewer_braces_expr_in_parens(): Unit = checkTree(
    """val a =
      |  xs
      |  (0)
      |""".stripMargin,
    """
      |ScalaFile
      |  ScPatternDefinition: a
      |    AnnotationsList
      |      <empty list>
      |    Modifiers
      |      <empty list>
      |    PsiElement(val)('val')
      |    PsiWhiteSpace(' ')
      |    ListOfPatterns
      |      ReferencePattern: a
      |        PsiElement(identifier)('a')
      |    PsiWhiteSpace(' ')
      |    PsiElement(=)('=')
      |    BlockExpression
      |      PsiWhiteSpace('\n  ')
      |      ReferenceExpression: xs
      |        PsiElement(identifier)('xs')
      |      PsiWhiteSpace('\n  ')
      |      ExpressionInParenthesis
      |        PsiElement(()('(')
      |        IntegerLiteral
      |          PsiElement(integer)('0')
      |        PsiElement())(')')
      |  PsiWhiteSpace('\n')
      |""".stripMargin
  )

  def test_function_type(): Unit = checkTree(
    """val q = (x: String => String) => x
      |""".stripMargin,
    """
      |ScalaFile
      |  ScPatternDefinition: q
      |    AnnotationsList
      |      <empty list>
      |    Modifiers
      |      <empty list>
      |    PsiElement(val)('val')
      |    PsiWhiteSpace(' ')
      |    ListOfPatterns
      |      ReferencePattern: q
      |        PsiElement(identifier)('q')
      |    PsiWhiteSpace(' ')
      |    PsiElement(=)('=')
      |    PsiWhiteSpace(' ')
      |    FunctionExpression
      |      Parameters
      |        ParametersClause
      |          PsiElement(()('(')
      |          Parameter: x
      |            AnnotationsList
      |              <empty list>
      |            PsiElement(identifier)('x')
      |            PsiElement(:)(':')
      |            PsiWhiteSpace(' ')
      |            ParameterType
      |              FunctionalType: String => String
      |                SimpleType: String
      |                  CodeReferenceElement: String
      |                    PsiElement(identifier)('String')
      |                PsiWhiteSpace(' ')
      |                PsiElement(=>)('=>')
      |                PsiWhiteSpace(' ')
      |                SimpleType: String
      |                  CodeReferenceElement: String
      |                    PsiElement(identifier)('String')
      |          PsiElement())(')')
      |      PsiWhiteSpace(' ')
      |      PsiElement(=>)('=>')
      |      PsiWhiteSpace(' ')
      |      ReferenceExpression: x
      |        PsiElement(identifier)('x')
      |  PsiWhiteSpace('\n')
      |""".stripMargin
  )

  def test_simple_type_annotation(): Unit = checkTree(
    """(1: Int)
      |""".stripMargin,
    """
      |ScalaFile
      |  ExpressionInParenthesis
      |    PsiElement(()('(')
      |    TypedExpression
      |      IntegerLiteral
      |        PsiElement(integer)('1')
      |      PsiElement(:)(':')
      |      PsiWhiteSpace(' ')
      |      SimpleType: Int
      |        CodeReferenceElement: Int
      |          PsiElement(identifier)('Int')
      |    PsiElement())(')')
      |  PsiWhiteSpace('\n')
      |""".stripMargin
  )
  //endregion
  // ------------------------------------------------------

}
