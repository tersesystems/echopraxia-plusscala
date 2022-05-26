package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api.{Condition => JCondition, FieldBuilderResult => JFieldBuilderResult, Level => JLevel, LoggingContext => JLoggingContext}

import java.util.stream
import java.util.stream.Collectors

trait LowPriorityImplicits {

  implicit class RichCondition(javaCondition: JCondition) {
    @inline
    def asScala: Condition = Condition((level, context) => javaCondition.test(level.asJava, context.asJava))
  }

  implicit class RichLoggingContext(context: JLoggingContext) {
    @inline
    def asScala: LoggingContext = new ScalaLoggingContext(context)
  }

  implicit class RichLevel(level: JLevel) {
    @inline
    def asScala: Level = Level.asScala(level)
  }

  implicit class RichFieldBuilderResult(result: JFieldBuilderResult) {
    @inline
    def concat(other: JFieldBuilderResult): JFieldBuilderResult = {
      () => stream.Stream.concat(result.fields().stream(), other.fields().stream()).collect(Collectors.toList())
    }

    @inline
    def ++(other: JFieldBuilderResult): JFieldBuilderResult = concat(other)
  }

}

object Implicits extends LowPriorityImplicits
