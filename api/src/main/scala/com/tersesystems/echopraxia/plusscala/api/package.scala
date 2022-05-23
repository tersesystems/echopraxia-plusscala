package com.tersesystems.echopraxia.plusscala

import com.tersesystems.echopraxia.api.{Condition => JCondition}
import com.tersesystems.echopraxia.api.{LoggingContext => JLoggingContext}
import com.tersesystems.echopraxia.api.{Level => JLevel}

package object api {

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
