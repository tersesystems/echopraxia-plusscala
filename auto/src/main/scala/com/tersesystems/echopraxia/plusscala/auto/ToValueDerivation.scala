package com.tersesystems.echopraxia.plusscala.auto

import com.tersesystems.echopraxia.api.{Field, Value}
import com.tersesystems.echopraxia.plusscala.api.ValueTypeClasses

import language.experimental.macros
import magnolia1._

import scala.jdk.CollectionConverters._

/**
 * This trait uses Magnolia to provide generic type class derivation
 * for case classes and sealed traits.  Note that you need to include
 * the `scala-reflect` library, see installation instructions.
 *
 * If you create a field builder that extends GenericValueTypeClasses,
 * then any case class that devolves down to the basic values is available:
 *
 * {{{
 * final case class IceCream(name: String, numCherries: Int, inCone: Boolean)
 *
 * final case class EntityId(raw: Int) extends AnyVal
 * final case class Bar(underlying: String) extends AnyVal
 * final case class Foo(bar: Bar)
 *
 * trait MagnoliaFieldBuilder extends FieldBuilder with ToValueDerivation
 *
 * object MagnoliaFieldBuilder extends MagnoliaFieldBuilder
 *
 * object MagnoliaMain {
 *   private val logger = LoggerFactory.getLogger.withFieldBuilder(MagnoliaFieldBuilder)
 *
 *   def main(args: Array[String]): Unit = {
 *     logger.info("{}", _.keyValue("icecream", IceCream("sundae", 1, false)))
 *     logger.info("{}", _.keyValue("entityId", EntityId(1)))
 *     logger.info("{}", _.keyValue("foo", Foo(Bar("underlying"))))
 *   }
 * }
 * }}}
 */
trait ToValueDerivation extends ValueTypeClasses {
  type Typeclass[T] = ToValue[T]

  def join[T](ctx: CaseClass[Typeclass, T]): Typeclass[T] = { value =>
    if (ctx.isValueClass) {
      val param = ctx.parameters.head
      param.typeclass.toValue(param.dereference(value))
    } else {
      val fields: Seq[Field] = ctx.parameters.map { p =>
        Field.keyValue(p.label, p.typeclass.toValue(p.dereference(value)))
      }
      Value.`object`(fields.asJava)
    }
  }

  def split[T](ctx: SealedTrait[Typeclass, T]): Typeclass[T] = (value: T) => {
    ctx.split(value) { sub =>
      sub.typeclass.toValue(sub.cast(value))
    }
  }

  implicit def gen[T]: Typeclass[T] = macro Magnolia.gen[T]
}
