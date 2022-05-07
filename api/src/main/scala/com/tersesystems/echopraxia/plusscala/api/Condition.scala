package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api.{Condition => JCondition, Level => JLevel, LoggingContext => JLoggingContext}

trait Condition {

  def test(level: Level, context: LoggingContext): Boolean

  def asJava: JCondition = { (level: JLevel, javaContext: JLoggingContext) =>
    this.test(Level.asScala(level), LoggingContext(javaContext))
  }
}

object Condition {

  def apply(f: (Level, LoggingContext) => Boolean): Condition = new Condition {
    override def test(level: Level, context: LoggingContext): Boolean = f(level, context)
  }

  def apply(f: LoggingContext => Boolean): Condition = new Condition {
    override def test(level: Level, context: LoggingContext): Boolean = f(context)
  }

}
