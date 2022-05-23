package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api.{Field, FieldConstants, LoggingContext => JLoggingContext}

import java.util
import scala.compat.java8.OptionConverters._
import scala.jdk.CollectionConverters._

object ScalaLoggingContext {
  // This repeats stuff in AbstractLoggingContext
  private val ExceptionPath = "$." + FieldConstants.EXCEPTION
}

/**
 * A scala logging context.
 */
class ScalaLoggingContext(context: JLoggingContext) extends LoggingContext {
  override def fields: Seq[Field] = {
    context.getFields.asScala.toSeq // needed for 2.13 since it's immutable
  }

  override def findString(jsonPath: String): Option[String] = {
    context.findString(jsonPath).asScala
  }

  override def findBoolean(jsonPath: String): Option[Boolean] = {
    context.findBoolean(jsonPath).asScala.map(_.booleanValue())
  }

  override def findNumber(jsonPath: String): Option[Number] = {
    context.findNumber(jsonPath).asScala
  }

  override def findNull(jsonPath: String): Boolean = {
    context.findNull(jsonPath)
  }

  override def findThrowable(jsonPath: String): Option[Throwable] = {
    context.findThrowable(jsonPath).asScala
  }

  override def findThrowable: Option[Throwable] = {
    findThrowable(ScalaLoggingContext.ExceptionPath)
  }

  override def findObject(jsonPath: String): Option[Map[String, Any]] = {
    context.findObject(jsonPath).asScala.map(deepAsScala(_))
  }

  override def findList(jsonPath: String): Seq[Any] = {
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

  override def asJava: JLoggingContext = context
}
