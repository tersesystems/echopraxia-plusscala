package com.tersesystems.echopraxia.plusscala

import com.tersesystems.echopraxia.api.FieldBuilderResult
import com.tersesystems.echopraxia.plusscala.api.{Condition, Level}
import com.tersesystems.echopraxia.api.{Level => JLevel}
import com.tersesystems.echopraxia.plusscala.spi.DefaultMethodsSupport
import com.tersesystems.echopraxia.spi.CoreLogger

abstract class ApplyLogger[FB, MT[_]] extends DefaultMethodsSupport[FB] {
  type MethodType = MT[FB]

  val error: MethodType = createMethod(Level.ERROR.asJava)
  val info: MethodType = createMethod(Level.INFO.asJava)
  val debug: MethodType = createMethod(Level.DEBUG.asJava)

  protected def createMethod(level: JLevel): MethodType
}

class SimpleLogger[FB, MT[_] <: ApplyMethods[_]](val name: String, val core: CoreLogger, val fieldBuilder: FB) extends ApplyLogger[FB, MT] {

  override protected def createMethod(level: JLevel): MethodType = new DefaultApplyMethods(level)

  class DefaultApplyMethods(level: JLevel) extends AbstractApplyMethods {
    override def apply(message: String): Unit = handleMessage(level, message)

    override def apply(condition: Condition, message: String): Unit = handleConditionMessage(level, condition, message)

    override def apply(message: String, f: FB => FieldBuilderResult): Unit = handleMessageArgs(level, message, f)

    override def apply(condition: Condition, message: String, f: FB => FieldBuilderResult): Unit = handleConditionMessageArgs(level, condition, message, f)
  }

  abstract class AbstractApplyMethods extends ApplyMethods[FB] with LoggerMethodSupport[FB] with DefaultMethodsSupport[FB] {
    override def name: String = SimpleLogger.this.name

    override def core: CoreLogger = SimpleLogger.this.core

    override def fieldBuilder: FB = SimpleLogger.this.fieldBuilder
  }
}