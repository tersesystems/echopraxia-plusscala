package echopraxia.plusscala.logging.api

import com.tersesystems.echopraxia.plusscala.api.FindPathMethods

object JsonPathCondition {
  def apply(f: (Level, LoggingContext with FindPathMethods) => Boolean): JsonPathCondition = new JsonPathCondition {

    override def jsonPathTest(level: Level, context: LoggingContext with FindPathMethods): Boolean = f(level, context)
  }

  def apply(f: (LoggingContext with FindPathMethods) => Boolean): JsonPathCondition = new JsonPathCondition {
    override def jsonPathTest(level: Level, context: LoggingContext with FindPathMethods): Boolean = f(context)
  }
}

trait JsonPathCondition extends Condition {
  def jsonPathTest(level: Level, context: LoggingContext with FindPathMethods): Boolean

  override def test(level: Level, context: LoggingContext): Boolean = {
    jsonPathTest(level, context.asInstanceOf[LoggingContext with FindPathMethods])
  }
}
