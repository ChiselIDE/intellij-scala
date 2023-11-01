package org.jetbrains.jps.incremental.scala.data

import org.jetbrains.jps.incremental.scala.Extractor
import org.jetbrains.jps.incremental.scala.data.ArgumentsParser.ArgumentsParserError
import org.jetbrains.plugins.scala.compiler.data._
import org.jetbrains.plugins.scala.compiler.data.serialization.{SerializationUtils, WorksheetArgsSerializer}

import java.io.File

// TODO: move to compiler-shared
//  unify with serializers org.jetbrains.plugins.scala.compiler.data.serialization.ArgListSerializer
trait ArgumentsParser {
  /** @see [[org.jetbrains.plugins.scala.compiler.data.Arguments.asStrings]] */
  def parse(string: Seq[String]): Either[ArgumentsParserError, Arguments]
}

object ArgumentsParser
  extends ArgumentsParser {

  case class ArgumentsParserError(message: String) extends RuntimeException(message)

  override def parse(strings: Seq[String]): Either[ArgumentsParserError, Arguments] = strings match {
    case Seq(
      PathToFile(pluginJpsDirectory),
      PathToFile(interfacesHome),
      javaClassVersion,
      StringToOption(compilerJarPaths),
      StringToOption(customCompilerBridgeJarPath),
      StringToOption(javaHomePath),
      PathsToFiles(sources),
      PathsToFiles(classpath),
      PathToFile(output),
      StringToSequence(scalaOptions),
      StringToSequence(javaOptions),
      order,
      PathToFile(cacheFile),
      PathsToFiles(outputs),
      PathsToFiles(caches),
      incrementalTypeName,
      PathsToFiles(sourceRoots),
      PathsToFiles(outputDirs),
      StringToSequence(worksheetArgsRaw),
      PathsToFiles(allSources),
      startDate,
      StringToBoolean(isCompile)
    ) =>

      def error(message: String): Left[ArgumentsParserError, Nothing] = Left(ArgumentsParserError(message))

      val SbtData.Jars(sbtInterfaceJar, compilerInterfaceJar, compilerBridges) =
        SbtData.Jars.fromPluginJpsDirectory(pluginJpsDirectory.toPath)
      val sbtData = SbtData(sbtInterfaceJar, compilerInterfaceJar, compilerBridges, interfacesHome, javaClassVersion)

      val compilerJars = compilerJarPaths.map {
        case PathsToFiles(files) =>
          val compilerBridgeJar = customCompilerBridgeJarPath.map(PathToFile)
          CompilerJarsFactory.fromFiles(files, compilerBridgeJar) match {
            case Left(resolveError) => return error(s"Couldn't extract compiler jars from: ${files.mkString(";")}\n" + resolveError.toString)
            case Right(value)       => value
          }
      }

      val javaHome = javaHomePath.map {
        case PathToFile(file) => file
      }

      val incrementalType = IncrementalityType.valueOf(incrementalTypeName)

      val compilerData = CompilerData(compilerJars, javaHome, incrementalType)

      val outputToCacheMap = outputs.zip(caches).toMap

      val outputGroups = sourceRoots zip outputDirs

      val zincData = ZincData(allSources, startDate.toLong, isCompile)

      val compilationData = CompilationData(
        sources,
        classpath,
        output,
        scalaOptions,
        javaOptions,
        CompileOrder.valueOf(order),
        cacheFile,
        outputToCacheMap,
        outputGroups,
        zincData
      )

      // this is actually not the best solution because we can't distinguish between empty sequence and absence of sequence,
      //  but will do for now, until we refactor communication protocol between client and server
      val worksheetArgs =
        if (worksheetArgsRaw.isEmpty) None
        else WorksheetArgsSerializer.deserialize(worksheetArgsRaw) match {
          case Right(value) => Option(value)
          case Left(errors) => return error(s"Couldn't parse worksheet arguments:\n${errors.mkString("\n")}")
        }

      Right(Arguments(sbtData, compilerData, compilationData, worksheetArgs))
  }

  private val PathToFile: Extractor[String, File] = new File(_)

  private val PathsToFiles: Extractor[String, Seq[File]] = { paths =>
    if (paths.isEmpty) Seq.empty else paths.split(SerializationUtils.Delimiter).map(PathToFile).toSeq
  }

  private val StringToOption: Extractor[String, Option[String]] = { s =>
    if (s.isEmpty) None else Some(s)
  }

  private val StringToSequence: Extractor[String, Seq[String]] = SerializationUtils.stringToSequence

  private val StringToBoolean: Extractor[String, Boolean] = _.toBoolean
}
