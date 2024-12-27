package echopraxia.plusscala.logging.api

import echopraxia.api.Value.ArrayValue
import echopraxia.api.Value.ObjectValue
import echopraxia.logging.api.{Condition => JCondition}
import echopraxia.logging.api.{Level => JLevel}
import echopraxia.logging.api.{LoggingContext => JLoggingContext}

trait Condition {

  def test(level: Level, context: LoggingContext): Boolean

  /**
   * Returns a condition which does a logical AND on this condition with the given condition.
   *
   * @param c
   *   the given condition.
   * @return
   *   a condition that renders result of this condition AND given condition.
   */
  def and(c: Condition): Condition = {
    c match {
      case Condition.always =>
        Condition.always
      case Condition.never =>
        Condition.never
      case other =>
        (level: Level, context: LoggingContext) => this.test(level, context) && other.test(level, context)
    }
  }

  /**
   * Returns a condition which does a logical AND on this condition with the given condition.
   *
   * @param c
   *   the given condition.
   * @return
   *   a condition that renders result of this condition OR given condition.
   */
  def or(c: Condition): Condition = {
    c match {
      case Condition.always =>
        Condition.always
      case Condition.never =>
        this
      case other =>
        (level: Level, context: LoggingContext) => this.test(level, context) || other.test(level, context)
    }
  }

  /**
   * Returns a condition which does a logical XOR on this condition with the given condition.
   *
   * @param c
   *   the given condition.
   * @return
   *   a condition that renders result of this condition XOR given condition.
   */
  def xor(c: Condition): Condition = { (level: Level, context: LoggingContext) =>
    this.test(level, context) ^ c.test(level, context)
  }

  def asJava: JCondition = javaCondition

  private lazy val javaCondition: JCondition = { (level: JLevel, javaContext: JLoggingContext) =>
    this.test(level.asScala, javaContext.asScala)
  }
}

object Condition {

  val always: Condition = new Condition {
    override def test(level: Level, context: LoggingContext): Boolean = true

    override def asJava: JCondition = JCondition.always()
  }

  val never: Condition = new Condition {
    override def test(level: Level, context: LoggingContext): Boolean = false

    override def asJava: JCondition = JCondition.never()
  }

  val diagnostic: Condition = (level: Level, _: LoggingContext) => level.isLessOrEqual(Level.DEBUG)

  val operational: Condition = (level: Level, _: LoggingContext) => level.isGreaterOrEqual(Level.INFO)

  def booleanMatch(fieldName: String, f: Boolean => Boolean): Condition = {
    JCondition.booleanMatch(fieldName, t => f(t.raw())).asScala
  }

  def numberMatch(fieldName: String, f: Number => Boolean): Condition = {
    JCondition.numberMatch(fieldName, t => f(t.asNumber().raw())).asScala
  }

  def stringMatch(fieldName: String, f: String => Boolean): Condition = {
    JCondition.stringMatch(fieldName, p => f(p.raw())).asScala
  }

  def nullMatch(fieldName: String): Condition = {
    JCondition.nullMatch(fieldName).asScala
  }

  def objectMatch(fieldName: String, f: ObjectValue => Boolean): Condition = {
    JCondition.objectMatch(fieldName, p => f(p.asObject())).asScala
  }

  def arrayMatch(fieldName: String, f: ArrayValue => Boolean): Condition = {
    JCondition.arrayMatch(fieldName, p => f(p.asArray())).asScala
  }

  def exactly(exactLevel: Level): Condition = (level: Level, _: LoggingContext) => level.isEqual(exactLevel)

  def apply(f: (Level, LoggingContext) => Boolean): Condition = new Condition {
    override def test(level: Level, context: LoggingContext): Boolean = f(level, context)
  }

  def apply(f: LoggingContext => Boolean): Condition = new Condition {
    override def test(level: Level, context: LoggingContext): Boolean = f(context)
  }

}
