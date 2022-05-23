package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api.{Condition => JCondition}
import com.tersesystems.echopraxia.api.{LoggingContext => JLoggingContext}
import com.tersesystems.echopraxia.api.{Level => JLevel}

trait LowPriorityImplicits {

  implicit class RichCondition(javaCondition: JCondition) {
    def asScala: Condition = Condition((level, context) => javaCondition.test(level.asJava, context.asJava))
  }

  implicit class RichLoggingContext(context: JLoggingContext) {
    def asScala: LoggingContext = new ScalaLoggingContext(context)
  }

  implicit class RichLevel(level: JLevel) {
    def asScala: Level = Level.asScala(level)
  }

}

object Implicits extends LowPriorityImplicits
