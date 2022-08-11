package com.tersesystems.echopraxia.plusscala.nameof

import com.tersesystems.echopraxia.api.{CoreLogger, FieldBuilderResult, Utilities}
import com.tersesystems.echopraxia.plusscala.api.{AbstractLoggerSupport, Condition, FieldBuilder, LoggerSupport}

import scala.annotation.tailrec
import scala.compat.java8.FunctionConverters.enrichAsJavaFunction
import scala.reflect.macros.blackbox
import scala.language.experimental.macros

/**
 * This class dumps a variable or field that has a `fieldBuilder.ToValue[A]` type class with it,
 * using the name of the field.
 *
 * It's useful for debugging situations where you just want to log some internal state ASAP.
 *
 * @param core the core logger
 * @param fieldBuilder the field builder
 * @tparam FB the field builder type.
 */
class NameOfLogger[FB <: FieldBuilder](core: CoreLogger, fieldBuilder: FB)
  extends AbstractLoggerSupport(core, fieldBuilder) with LoggerSupport[FB] {
  import NameOfLogger.impl

  def trace[A](expr: A): Unit = macro impl.trace[A]
  def debug[A](expr: A): Unit = macro impl.debug[A]
  def info[A](expr: A): Unit = macro impl.info[A]
  def warn[A](expr: A): Unit = macro impl.warn[A]
  def error[A](expr: A): Unit = macro impl.error[A]

  def withCondition(condition: Condition): NameOfLogger[FB] = {
    condition match {
      case Condition.always =>
        this
      case Condition.never =>
        new NeverLogger(core, fieldBuilder)
      case other =>
        newLogger(newCoreLogger = core.withCondition(other.asJava))
    }
  }

  def withFields(f: FB => FieldBuilderResult): NameOfLogger[FB] = {
    newLogger(newCoreLogger = core.withFields(f.asJava, fieldBuilder))
  }

  def withThreadContext: NameOfLogger[FB] = {
    newLogger(
      newCoreLogger = core.withThreadContext(Utilities.threadContext())
    )
  }

  def withFieldBuilder[NEWFB <: FieldBuilder](newFieldBuilder: NEWFB): NameOfLogger[NEWFB] = {
    newLogger(newFieldBuilder = newFieldBuilder)
  }

  @inline
  private def newLogger[T <: FieldBuilder](
                            newCoreLogger: CoreLogger = core,
                            newFieldBuilder: T = fieldBuilder
                          ): NameOfLogger[T] =
    new NameOfLogger[T](newCoreLogger, newFieldBuilder)

  class NeverLogger(core: CoreLogger, fieldBuilder: FB) extends NameOfLogger[FB](core: CoreLogger, fieldBuilder: FB) {
    override def trace[A](expr: A): Unit = macro impl.nothing[A]
    override def debug[A](expr: A): Unit = macro impl.nothing[A]
    override def info[A](expr: A): Unit = macro impl.nothing[A]
    override def warn[A](expr: A): Unit = macro impl.nothing[A]
    override def error[A](expr: A): Unit = macro impl.nothing[A]
  }
}

object NameOfLogger {

  private class impl(val c: blackbox.Context) {
    import c.universe._

    def nothing[A: c.WeakTypeTag](expr: c.Tree): Tree = {
      q"(): Unit"
    }

    def error[A: c.WeakTypeTag](expr: c.Tree) = {
      val tpeA: Type = implicitly[WeakTypeTag[A]].tpe
      val level: Select = q"com.tersesystems.echopraxia.api.Level.ERROR"

      handle(tpeA, level, expr)
    }

    def warn[A: c.WeakTypeTag](expr: c.Tree) = {
      val tpeA: Type = implicitly[WeakTypeTag[A]].tpe
      val level: Select = q"com.tersesystems.echopraxia.api.Level.WARN"

      handle(tpeA, level, expr)
    }

    def info[A: c.WeakTypeTag](expr: c.Tree) = {
      val tpeA: Type = implicitly[WeakTypeTag[A]].tpe
      val level: Select = q"com.tersesystems.echopraxia.api.Level.INFO"

      handle(tpeA, level, expr)
    }

    def debug[A: c.WeakTypeTag](expr: c.Tree) = {
      val tpeA: Type = implicitly[WeakTypeTag[A]].tpe
      val level: Select = q"com.tersesystems.echopraxia.api.Level.DEBUG"

      handle(tpeA, level, expr)
    }

    def trace[A: c.WeakTypeTag](expr: c.Tree) = {
      val tpeA: Type = implicitly[WeakTypeTag[A]].tpe
      val level: Select = q"com.tersesystems.echopraxia.api.Level.TRACE"

      handle(tpeA, level, expr)
    }

    private def handle(tpeA: Type, level: Select, expr: c.Tree) = {
      // taken from https://github.com/dwickern/scala-nameof
      @tailrec def extract(tree: c.Tree): String = tree match {
        case Ident(n) => n.decodedName.toString
        case Select(_, n) => n.decodedName.toString
        case Function(_, body) => extract(body)
        case Block(_, expr) => extract(expr)
        case Apply(func, _) => extract(func)
        case TypeApply(func, _) => extract(func)
        case _ =>
          c.abort(c.enclosingPosition, s"Unsupported expression: ${expr}")
      }

      val name = expr match {
        case Literal(Constant(_)) => c.abort(c.enclosingPosition, "Cannot provide name to static constant!")
        case _ => extract(expr)
      }

      val logger = c.prefix
      val fieldBuilderType = tq"$logger.fieldBuilder.type"
      val function = q"""new java.util.function.Function[$fieldBuilderType, com.tersesystems.echopraxia.api.FieldBuilderResult]() {
        def apply(fb: $fieldBuilderType) = fb.keyValue($name, fb.ToValue[$tpeA]($expr))
      }"""
      q"""$logger.core.log($level, "{}", $function, $logger.fieldBuilder)"""
    }
  }
}
