package com.tersesystems.echopraxia.plusscala.api

import com.tersesystems.echopraxia.api.{Field, Value}

import language.experimental.macros
import magnolia1._

import scala.jdk.CollectionConverters._
import scala.language.experimental.macros

/**
 * This trait uses Magnolia to provide generic type class derivation
 * for case classes and sealed traits.  Note that you need to include
 * the `scala-reflect` library to use it, see installation instructions.
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
 * trait MagnoliaFieldBuilder extends FieldBuilder with AutoDerivation
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
sealed trait Derivation extends ValueTypeClasses {
  type Typeclass[T] = ToValue[T]

  // leave this as public so we can access the macro "stack trace" when calling
  //     AutoFieldBuilder.gen[Option[Instant]]
  // for debugging purposes:
  // https://github.com/softwaremill/magnolia/tree/scala2#debugging
  final def join[T](ctx: CaseClass[Typeclass, T]): Typeclass[T] = {
      if (ctx.isValueClass) {
        val param = ctx.parameters.head
        value => param.typeclass.toValue(param.dereference(value))
      } else if (ctx.isObject) { // a case object


        // https://www.lyh.me/magnolify.html
        // https://github.com/scanamo/scanamo/pull/538
        // https://github.com/softwaremill/magnolia/issues/267
        // Magnolify

        // logger.info("{}", _.keyValue("none", None)
        // how do I say there's no derivation here?
        //    {
        //      if (ctx.parameters.isEmpty) {
        //        // if this is a None, render null.
        //        // if this is an array, render an empty array.
        //        // if this is an empty tuple, what do?
        //        Value.ArrayValue.EMPTY
        //      } else {
        //        throw new IllegalStateException(s"${ctx.typeName}")
        //      }
        //    }
        value => Value.exception(new IllegalStateException(s"${ctx.typeName} for ${value}"))
      } else { // this is a regular case class
        value => {
          val fields: Seq[Field] = ctx.parameters.map { p =>
            Field.keyValue(p.label, p.typeclass.toValue(p.dereference(value)))
          }
          val typeInfoField = Field.keyValue("@type", Value.string(ctx.typeName.full))
          Value.`object`((fields :+ typeInfoField).asJava)
        }
      }
  }

  final def split[T](ctx: SealedTrait[Typeclass, T]): Typeclass[T] = (value: T) => {
    ctx.split(value) { sub =>
      sub.typeclass.toValue(sub.cast(value))
    }
  }
}

/**
 * Fully automatic derivation.
 */
trait AutoDerivation extends Derivation {
  final implicit def gen[T]: Typeclass[T] = macro Magnolia.gen[T]
}

/**
 * Semi automatic derivation.
 */
trait SemiAutoDerivation extends Derivation {
  final def gen[T]: Typeclass[T] = macro Magnolia.gen[T]
}
