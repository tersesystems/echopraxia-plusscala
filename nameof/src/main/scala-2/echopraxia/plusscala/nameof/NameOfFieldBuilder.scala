package echopraxia.plusscala.nameof

import echopraxia.api.Field
import echopraxia.plusscala.api.FieldBuilder

import scala.annotation.tailrec
import scala.language.experimental.macros
import scala.reflect.macros.blackbox

trait NameOfFieldBuilder extends FieldBuilder {
  import NameOfFieldBuilder.impl

  def nameOf(expr: Any): String = macro impl.nameOf

  def nameOfKeyValue[A](expr: A): Field = macro impl.keyValue[A]

  def nameOfValue[A](expr: A): Field = macro impl.keyValue[A]
}

object NameOfFieldBuilder extends NameOfFieldBuilder {

  private class impl(val c: blackbox.Context) {
    import c.universe._

    def nameOf(expr: c.Expr[Any]): c.Expr[String] = {
      import c.universe._
      @tailrec def extract(tree: c.Tree): String = tree match {
        case Ident(n)           => n.decodedName.toString
        case Select(_, n)       => n.decodedName.toString
        case Function(_, body)  => extract(body)
        case Block(_, expr)     => extract(expr)
        case Apply(func, _)     => extract(func)
        case TypeApply(func, _) => extract(func)
        case _ =>
          c.abort(c.enclosingPosition, s"Unsupported expression: ${expr.tree}")
      }

      val name = extract(expr.tree)
      c.Expr[String](q"$name")
    }

    def keyValue[A: c.WeakTypeTag](expr: c.Tree): c.Tree = {
      val tpeA: Type = implicitly[WeakTypeTag[A]].tpe
      // taken from https://github.com/dwickern/scala-nameof
      @tailrec def extract(tree: c.Tree): String = tree match {
        case Ident(n)           => n.decodedName.toString
        case Select(_, n)       => n.decodedName.toString
        case Function(_, body)  => extract(body)
        case Block(_, expr)     => extract(expr)
        case Apply(func, _)     => extract(func)
        case TypeApply(func, _) => extract(func)
        case _ =>
          c.abort(c.enclosingPosition, s"Unsupported expression: ${expr}")
      }
      val name = expr match {
        case Literal(Constant(_)) => c.abort(c.enclosingPosition, "Cannot provide name to static constant!")
        case _                    => extract(expr)
      }

      q"""(${c.prefix}.keyValue($name, fb.ToValue[$tpeA]($expr)))"""
    }

    def value[A: c.WeakTypeTag](expr: c.Tree): c.Tree = {
      val tpeA: Type = implicitly[WeakTypeTag[A]].tpe
      // taken from https://github.com/dwickern/scala-nameof
      @tailrec def extract(tree: c.Tree): String = tree match {
        case Ident(n)           => n.decodedName.toString
        case Select(_, n)       => n.decodedName.toString
        case Function(_, body)  => extract(body)
        case Block(_, expr)     => extract(expr)
        case Apply(func, _)     => extract(func)
        case TypeApply(func, _) => extract(func)
        case _ =>
          c.abort(c.enclosingPosition, s"Unsupported expression: ${expr}")
      }
      val name = expr match {
        case Literal(Constant(_)) => c.abort(c.enclosingPosition, "Cannot provide name to static constant!")
        case _                    => extract(expr)
      }

      q"""(${c.prefix}.value($name, fb.ToValue[$tpeA]($expr)))"""
    }
  }

}
