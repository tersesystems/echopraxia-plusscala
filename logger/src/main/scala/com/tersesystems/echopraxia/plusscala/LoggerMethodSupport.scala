package com.tersesystems.echopraxia.plusscala

import com.tersesystems.echopraxia.api.{Field, FieldBuilderResult, Level, Value}
import com.tersesystems.echopraxia.plusscala.api.Condition
import com.tersesystems.echopraxia.plusscala.spi.DefaultMethodsSupport
import com.tersesystems.echopraxia.spi.FieldConstants

import scala.compat.java8.FunctionConverters._

trait LoggerMethodSupport[FB] {
  this: DefaultMethodsSupport[FB] =>

  // -----------------------------------------------------------
  // Internal methods

  @inline
  protected def handleMessage(level: Level, message: String): Unit = {
    core.log(level, message)
  }

  @inline
  protected def handleMessageArgs(level: Level, message: String, f: FB => FieldBuilderResult): Unit = {
    core.log(level, message, f.asJava, fieldBuilder)
  }

  @inline
  protected def handleMessageThrowable(level: Level, message: String, e: Throwable): Unit = {
    core.log(level, message, (_: FB) => onlyException(e), fieldBuilder)
  }

  @inline
  protected def handleConditionMessage(level: Level, condition: Condition, message: String): Unit = {
    core.log(level, condition.asJava, message)
  }

  @inline
  protected def handleConditionMessageArgs(level: Level, condition: Condition, message: String, f: FB => FieldBuilderResult): Unit = {
    core.log(level, condition.asJava, message, f.asJava, fieldBuilder)
  }

  @inline
  protected def handleConditionMessageThrowable(level: Level, condition: Condition, message: String, e: Throwable): Unit = {
    core.log(level, condition.asJava, message, (_: FB) => onlyException(e), fieldBuilder)
  }

  @inline
  protected def onlyException(e: Throwable): FieldBuilderResult = {
    Field.keyValue(FieldConstants.EXCEPTION, Value.exception(e))
  }

}
