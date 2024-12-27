package echopraxia.plusscala.logging.api

import echopraxia.logging.api.{Condition => JCondition}
import echopraxia.logging.api.{Level => JLevel}
import echopraxia.logging.api.{LoggingContext => JLoggingContext}
import echopraxia.logging.api.{LoggingContextWithFindPathMethods => JLoggingContextWithFindPathMethods}

trait LowPriorityImplicits {

  final implicit class RichCondition(javaCondition: JCondition) {
    @inline
    def asScala: Condition = Condition((level, context) => javaCondition.test(level.asJava, context.asJava))
  }

  final implicit class RichLoggingContext(context: JLoggingContext) {
    @inline
    def asScala: LoggingContext = {
      context match {
        case ctxWithFindPathMethods: JLoggingContextWithFindPathMethods =>
          new ScalaLoggingContextWithFindPathMethods(ctxWithFindPathMethods)
        case plainContext: JLoggingContext =>
          new ScalaLoggingContext(plainContext)
      }
    }
  }

  final implicit class RichLevel(level: JLevel) {
    @inline
    def asScala: Level = Level.asScala(level)
  }
}
