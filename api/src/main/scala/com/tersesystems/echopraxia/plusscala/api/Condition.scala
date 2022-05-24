package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api.{Condition => JCondition, Level => JLevel, LoggingContext => JLoggingContext}

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

  def asJava: JCondition = { (level: JLevel, javaContext: JLoggingContext) =>
    this.test(level.asScala, javaContext.asScala)
  }
}

object Condition {

  val always: Condition = (level: Level, context: LoggingContext) => true

  val never: Condition = (level: Level, context: LoggingContext) => false

  val diagnostic: Condition = (level: Level, context: LoggingContext) => level.isLessOrEqual(Level.DEBUG)

  val operational: Condition = (level: Level, context: LoggingContext) => level.isGreaterOrEqual(Level.INFO)

  def exactly(exactLevel: Level): Condition = (level: Level, context: LoggingContext) => level.isEqual(exactLevel)

  def apply(f: (Level, LoggingContext) => Boolean): Condition = new Condition {
    override def test(level: Level, context: LoggingContext): Boolean = f(level, context)
  }

  def apply(f: LoggingContext => Boolean): Condition = new Condition {
    override def test(level: Level, context: LoggingContext): Boolean = f(context)
  }

}
