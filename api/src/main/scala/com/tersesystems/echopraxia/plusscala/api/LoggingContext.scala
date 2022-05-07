package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api.{FieldConstants, LoggingContext => JLoggingContext}

import java.util
import scala.compat.java8.OptionConverters._

import scala.jdk.CollectionConverters._

object LoggingContext {

  // This repeats stuff in AbstractLoggingContext
  private val ExceptionPath = "$." + FieldConstants.EXCEPTION

  def apply(context: JLoggingContext): LoggingContext = {
    new LoggingContext(context)
  }
}

/**
 * A scala logging context.
 */
class LoggingContext private (context: JLoggingContext) {

  def findString(jsonPath: String): Option[String] = {
    context.findString(jsonPath).asScala
  }

  def findBoolean(jsonPath: String): Option[Boolean] = {
    context.findBoolean(jsonPath).asScala.map(_.booleanValue())
  }

  def findNumber(jsonPath: String): Option[Number] = {
    context.findNumber(jsonPath).asScala
  }

  def findNull(jsonPath: String): Boolean = {
    context.findNull(jsonPath)
  }

  def findThrowable(jsonPath: String): Option[Throwable] = {
    context.findThrowable(jsonPath).asScala
  }

  def findThrowable: Option[Throwable] = {
    findThrowable(LoggingContext.ExceptionPath)
  }

  def findObject(jsonPath: String): Option[Map[String, Any]] = {
    context.findObject(jsonPath).asScala.map(deepAsScala(_))
  }

  def findList(jsonPath: String): Seq[Any] = {
    deepAsScala(context.findList(jsonPath))
  }

  private def deepAsScala(value: util.List[_]): Seq[Any] = {
    value.asScala.map {
      case javaMap: util.Map[_, _] =>
        deepAsScala(javaMap)
      case list: util.List[_] =>
        deepAsScala(list)
      case other =>
        other
    }.toSeq
  }

  private def deepAsScala(value: util.Map[_, _]): Map[String, Any] = {
    value.asScala
      .map {
        case (k, v: util.List[_]) =>
          k -> deepAsScala(v)
        case (k, v: util.Map[_, _]) =>
          k -> deepAsScala(v)
        case (k, v) =>
          k -> deepAsScalaValue(v)
      }
      .toMap
      .asInstanceOf[Map[String, Any]]
  }

  private def deepAsScalaValue(value: Any): Any = {
    value match {
      case bigDec: java.math.BigDecimal =>
        scala.BigDecimal(bigDec)
      case bigInt: java.math.BigInteger =>
        scala.BigInt(bigInt)
      case other =>
        other
    }
  }

}
