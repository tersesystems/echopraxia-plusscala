package echopraxia.plusscala.logging.api

import echopraxia.plusscala.api.FindPathMethods

object JsonPathCondition {
  def apply(f: (Level, LoggingContext & FindPathMethods) => Boolean): JsonPathCondition = new JsonPathCondition {

    override def jsonPathTest(level: Level, context: LoggingContext & FindPathMethods): Boolean = f(level, context)
  }

  def apply(f: (LoggingContext & FindPathMethods) => Boolean): JsonPathCondition = new JsonPathCondition {
    override def jsonPathTest(level: Level, context: LoggingContext & FindPathMethods): Boolean = f(context)
  }
}

trait JsonPathCondition extends Condition {
  def jsonPathTest(level: Level, context: LoggingContext & FindPathMethods): Boolean

  override def test(level: Level, context: LoggingContext): Boolean = {
    jsonPathTest(level, context.asInstanceOf[LoggingContext & FindPathMethods])
  }
}
