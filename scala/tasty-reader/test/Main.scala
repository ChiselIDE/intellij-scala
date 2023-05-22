package org.jetbrains.plugins.scala.tasty.reader

import java.io.{BufferedInputStream, File, FileInputStream}
import java.nio.file.{Files, Path, Paths}
import java.util.jar.JarInputStream
import scala.util.chaining.scalaUtilChainingOps

object Main {
  enum Mode { case Parse, Test, Benchmark }

  private val mode = Mode.Test

  private val Home: String = System.getProperty("user.home")

  private val Repository = Home + "/.cache/coursier/v1/https/repo1.maven.org/maven2/"
  private val OutputDir = Home + "/IdeaProjects/scala-plugin-for-ultimate/community/scala/tasty-reader/target/comparison"

  // scalaVersion := "3.2.2",

  // libraryDependencies += "org.scala-lang" %% "scala3-compiler" % "3.2.2",

  // libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.14",

  // libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.17.0",

  // libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.14",

  // libraryDependencies += "dev.zio" %% "zio" % "2.0.2",
  // libraryDependencies += "dev.zio" %% "zio-streams" % "2.0.2",

  // libraryDependencies += "org.typelevel" %% "cats-core" % "2.8.0",
  // libraryDependencies += "org.typelevel" %% "cats-effect" % "3.3.14",
  // libraryDependencies += "org.typelevel" %% "cats-free" % "2.8.0",
  // libraryDependencies += "org.typelevel" %% "cats-laws" % "2.8.0",

  // libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.3.7",
  // libraryDependencies += "org.scalaz" %% "scalaz-effect" % "7.3.7",

  // libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.7.0",
  // libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % "2.7.0",
  // libraryDependencies += "com.typesafe.akka" %% "akka-cluster" % "2.7.0",
  // libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.5.0",
  // libraryDependencies += "com.typesafe.akka" %% "akka-http-core" % "10.5.0",
  // libraryDependencies += "com.typesafe.akka" %% "akka-persistence" % "2.7.0",

  // libraryDependencies += "co.fs2" %% "fs2-core" % "3.6.1",

  // libraryDependencies += "org.tpolecat" %% "doobie-core" % "1.0.0-RC1",

  // libraryDependencies += "io.getquill" %% "quill-sql" % "4.6.0",
  // libraryDependencies += "io.getquill" %% "quill-jdbc-zio" % "4.6.0"

  private val Libraries = Seq(
    "org/scala-lang/scala3-library_3/3.2.2/scala3-library_3-3.2.2.jar",
    "org/scala-lang/scala3-compiler_3/3.2.2/scala3-compiler_3-3.2.2.jar",

    "org/scalatest/scalatest-core_3/3.2.14/scalatest-core_3-3.2.14.jar",
    "org/scalatest/scalatest-diagrams_3/3.2.14/scalatest-diagrams_3-3.2.14.jar",
    "org/scalatest/scalatest-featurespec_3/3.2.14/scalatest-featurespec_3-3.2.14.jar",
    "org/scalatest/scalatest-flatspec_3/3.2.14/scalatest-flatspec_3-3.2.14.jar",
    "org/scalatest/scalatest-freespec_3/3.2.14/scalatest-freespec_3-3.2.14.jar",
    "org/scalatest/scalatest-funspec_3/3.2.14/scalatest-funspec_3-3.2.14.jar",
    "org/scalatest/scalatest-funsuite_3/3.2.14/scalatest-funsuite_3-3.2.14.jar",
    "org/scalatest/scalatest-matchers-core_3/3.2.14/scalatest-matchers-core_3-3.2.14.jar",
    "org/scalatest/scalatest-mustmatchers_3/3.2.14/scalatest-mustmatchers_3-3.2.14.jar",
    "org/scalatest/scalatest-propspec_3/3.2.14/scalatest-propspec_3-3.2.14.jar",
    "org/scalatest/scalatest-refspec_3/3.2.14/scalatest-refspec_3-3.2.14.jar",
    "org/scalatest/scalatest-shouldmatchers_3/3.2.14/scalatest-shouldmatchers_3-3.2.14.jar",
    "org/scalatest/scalatest-wordspec_3/3.2.14/scalatest-wordspec_3-3.2.14.jar",

    "org/scalactic/scalactic_3/3.2.14/scalactic_3-3.2.14.jar",

    "org/scalacheck/scalacheck_3/1.17.0/scalacheck_3-1.17.0.jar",

    "dev/zio/zio_3/2.0.2/zio_3-2.0.2.jar",
    "dev/zio/zio-streams_3/2.0.2/zio-streams_3-2.0.2.jar",

    "org/typelevel/cats-core_3/2.8.0/cats-core_3-2.8.0.jar",
    "org/typelevel/cats-effect_3/3.3.14/cats-effect_3-3.3.14.jar",
    "org/typelevel/cats-effect-kernel_3/3.3.14/cats-effect-kernel_3-3.3.14.jar",
    "org/typelevel/cats-effect-std_3/3.3.14/cats-effect-std_3-3.3.14.jar",
    "org/typelevel/cats-free_3/2.8.0/cats-free_3-2.8.0.jar",
    "org/typelevel/cats-kernel_3/2.8.0/cats-kernel_3-2.8.0.jar",
    "org/typelevel/cats-kernel-laws_3/2.8.0/cats-kernel-laws_3-2.8.0.jar",
    "org/typelevel/cats-laws_3/2.8.0/cats-laws_3-2.8.0.jar",

    "org/scalaz/scalaz-core_3/7.3.7/scalaz-core_3-7.3.7.jar",
    "org/scalaz/scalaz-effect_3/7.3.7/scalaz-effect_3-7.3.7.jar",

    "com/typesafe/akka/akka-actor_3/2.7.0/akka-actor_3-2.7.0.jar",
    "com/typesafe/akka/akka-actor-typed_3/2.7.0/akka-actor-typed_3-2.7.0.jar",
    "com/typesafe/akka/akka-coordination_3/2.7.0/akka-coordination_3-2.7.0.jar",
    "com/typesafe/akka/akka-cluster_3/2.7.0/akka-cluster_3-2.7.0.jar",
    "com/typesafe/akka/akka-http_3/10.5.0/akka-http_3-10.5.0.jar",
    "com/typesafe/akka/akka-http-core_3/10.5.0/akka-http-core_3-10.5.0.jar",
    "com/typesafe/akka/akka-persistence_3/2.7.0/akka-persistence_3-2.7.0.jar",
    "com/typesafe/akka/akka-parsing_3/10.5.0/akka-parsing_3-10.5.0.jar",
    "com/typesafe/akka/akka-remote_3/2.7.0/akka-remote_3-2.7.0.jar",
    "com/typesafe/akka/akka-stream_3/2.7.0/akka-stream_3-2.7.0.jar",

    "co/fs2/fs2-core_3/3.6.1/fs2-core_3-3.6.1.jar",

    "io/getquill/quill-sql_3/4.6.0/quill-sql_3-4.6.0.jar",
    "io/getquill/quill-jdbc-zio_3/4.6.0/quill-jdbc-zio_3-4.6.0.jar",

    "org/tpolecat/doobie-core_3/1.0.0-RC1/doobie-core_3-1.0.0-RC1.jar",
    "org/tpolecat/doobie-free_3/1.0.0-RC1/doobie-free_3-1.0.0-RC1.jar",
  )

  // TODO check for lexer & parser errors and unresolved references
  def main(args: Array[String]): Unit = {
    assert(new File(OutputDir).getParentFile.exists)

    val start = System.currentTimeMillis()

    Libraries.sortBy(_.split('/').last).foreach { binaries =>
      println("Parsing TASTy:\t\t" + binaries)
      new JarInputStream(new BufferedInputStream(new FileInputStream(Repository + "/" + binaries))).pipe { in =>
        Iterator.continually(in.getNextEntry).takeWhile(_ != null).filter(_.getName.endsWith(".tasty")).foreach { entry =>
          val file = new File(s"$OutputDir/${entry.getName}")
          val tree = TreeReader.treeFrom(in.readAllBytes())
          val path = Paths.get(file.getPath.replaceFirst("\\.tasty$", ".scala"))
          val treePrinter = new TreePrinter(legacySyntax = true)
          val fileAndText = try {
            treePrinter.fileAndTextOf(tree)
          } catch {
            case _: StackOverflowError => (file, "TODO")
          }
          mode match {
            case Mode.Parse =>
              file.getParentFile.mkdirs()
              //Files.write(Paths.get(file.getPath.replaceFirst("\\.tasty$", ".tree")), tree.toString.getBytes)
              Files.write(path, fileAndText._2.getBytes)
            case Mode.Test =>
              val expected = new String(Files.readAllBytes(path))
              val (_, actual) = fileAndText
              val actualPath = Path.of(path.toString.replaceFirst("\\.scala$", ".actual.scala"))
              if (expected != actual) {
                System.err.println(path.toString.substring(OutputDir.length + 1))
                Files.write(actualPath, actual.getBytes)
              } else {
                val actualFile = actualPath.toFile
                if (actualFile.exists()) {
                  actualFile.delete()
                }
              }
            case Mode.Benchmark =>
              fileAndText
          }
        }
      }

      val sources = binaries.replaceFirst("\\.jar$", "-sources.jar")

      if (mode == Mode.Parse) {
        println("Extracting sources:\t" + sources)
        new JarInputStream(new BufferedInputStream(new FileInputStream(Repository + "/" + sources))).pipe { in =>
          Iterator.continually(in.getNextEntry).takeWhile(_ != null).filter(_.getName.endsWith(".scala")).foreach { entry =>
            val file = new File(s"$OutputDir/${entry.getName}")
            file.getParentFile.mkdirs()
            val s = new String(in.readAllBytes) // TODO store pre-compiled regex
              .replaceAll(raw"(?m)^\s*import.*?$$", "") // Import
              .replaceAll(raw"\s*//.*?\n", "") // Line comment
              .replaceAll(raw"(?s)/\*.*?\*/", "") // Block comment
              .replaceAll(raw"(?m)^\s+$$", "") // Whitespaces on empty line
              .replaceAll(raw"\n{3,}", "\n\n") // Multiple empty lines
              .replaceAll(raw"\{\n\n", "{\n") // Empty line after {
              .replaceAll(raw"\n\n}", "\n}") // Empty line before }
              .trim

            Files.write(Paths.get(file.getPath.replaceFirst("\\.scala$", "-source.scala")), s.getBytes)
          }
        }
      }
    }

    if (mode == Mode.Benchmark) {
      printf("Elapsed: %.2f s.\n", (System.currentTimeMillis() - start) / 1000.0D)
    } else {
      println("Done:\t\t\t\t" + OutputDir)
    }
  }
}
