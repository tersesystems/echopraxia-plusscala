package echopraxia.plusscala.logging.api

import echopraxia.api.Field
import echopraxia.api.FieldConstants
import echopraxia.logging.api.{LoggingContext => JLoggingContext}
import echopraxia.logging.api.{LoggingContextWithFindPathMethods => JLoggingContextWithFindPathMethods}
import echopraxia.plusscala.api.FindPathMethods

import java.util
import scala.compat.java8.OptionConverters._
import scala.jdk.CollectionConverters._

/**
 * A scala logging context.
 */
class ScalaLoggingContext(context: JLoggingContext) extends LoggingContext {
  override lazy val fields: Seq[Field] = {
    context.getFields.asScala.toSeq // needed for 2.13 since it's immutable
  }

  override lazy val argumentFields: Seq[Field] = {
    context.getArgumentFields.asScala.toSeq
  }

  override lazy val loggerFields: Seq[Field] = {
    context.getLoggerFields.asScala.toSeq
  }

  override def asJava: JLoggingContext = context
}

class ScalaLoggingContextWithFindPathMethods(context: JLoggingContextWithFindPathMethods) extends ScalaLoggingContext(context) with FindPathMethods {

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
    findThrowable("$." + FieldConstants.EXCEPTION)
  }

  override def findObject(jsonPath: String): Option[Map[String, Any]] = {
    context.findObject(jsonPath).asScala.map(deepAsScalaMap(_))
  }

  override def findList(jsonPath: String): Seq[Any] = {
    deepAsScalaSeq(context.findList(jsonPath))
  }

  private def deepAsScalaSeq(value: util.List[_]): Seq[Any] = {
    value.asScala.map {
      case javaMap: util.Map[String, _] =>
        deepAsScalaMap(javaMap)
      case list: util.List[_] =>
        deepAsScalaSeq(list)
      case other =>
        other
    }.toSeq
  }

  private def deepAsScalaMap(value: util.Map[String, _]): Map[String, Any] = {
    value.asScala.map { case (k: String, v) =>
      val mappedV = v match {
        case utilList: util.List[_] =>
          deepAsScalaSeq(utilList)
        case (utilMap: util.Map[String, _]) =>
          deepAsScalaMap(utilMap)
        case other =>
          deepAsScalaValue(other)
      }
      k -> mappedV
    }.toMap
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
