package echopraxia.plusscala.generic

import echopraxia.api.Field
import echopraxia.api.Value
import echopraxia.plusscala.api.ValueTypeClasses
import magnolia1._

import java.util.Objects
import scala.language.experimental.macros

/**
 * This trait uses Magnolia to provide generic type class derivation for case classes and sealed traits. Note that you need to include the
 * `scala-reflect` library to use it, see installation instructions.
 *
 * If you create a field builder that uses derivation, it will automatically log case classes where all the attributes have `ToValue` type classes in
 * scope.
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

  type CaseClass[T]   = magnolia1.CaseClass[Typeclass, T]
  type SealedTrait[T] = magnolia1.SealedTrait[Typeclass, T]

  // https://github.com/scanamo/scanamo/pull/538
  // https://github.com/softwaremill/magnolia/issues/267
  // https://github.com/spotify/magnolify
  // https://www.lyh.me/magnolify.html
  // https://github.com/vpavkin/circe-magnolia/blob/master/derivation/src/main/scala/io/circe/magnolia/MagnoliaDecoder.scala

  // logger.info("{}", _.keyValue("none", None)
  // how do I say there's no derivation here?  Just leave the default?
  // fallback: ensure that field builders can special case using the protected join* methods

  // leave this as public so we can access the macro "stack trace" when calling
  //     AutoFieldBuilder.gen[Option[Instant]]
  // for debugging purposes:
  // https://github.com/softwaremill/magnolia/tree/scala2#debugging
  final def join[T](ctx: CaseClass[T]): Typeclass[T] = {
    if (ctx.isValueClass) {
      joinValueClass(ctx)
    } else if (ctx.isObject) {
      joinCaseObject(ctx)
    } else {
      joinCaseClass(ctx)
    }
  }

  // this is a regular case class
  protected def joinCaseClass[T](ctx: CaseClass[T]): Typeclass[T] = { obj =>
    val typeInfo = Field.keyValue("@type", ToValue(ctx.typeName.full))
    val fields: Seq[Field] = ctx.parameters.map { p =>
      val name: String      = p.label
      val attribute         = p.dereference(obj)
      val typeclassInstance = Objects.requireNonNull(p.typeclass, "Null typeclass!  Please see https://github.com/softwaremill/magnolia/issues/402")
      val value: Value[_]   = typeclassInstance.toValue(attribute)
      Field.keyValue(name, value)
    }
    ToObjectValue(typeInfo +: fields)
  }

  // this is a case object, we can't do anything with it.
  protected def joinCaseObject[T](ctx: CaseClass[T]): Typeclass[T] = {
    // ctx has no parameters, so we're better off just passing it straight through.
    value => Value.string(value.toString)
  }

  // this is a value class aka AnyVal, we should pass it through.
  protected def joinValueClass[T](ctx: CaseClass[T]): Typeclass[T] = {
    val param = ctx.parameters.head
    value => param.typeclass.toValue(param.dereference(value))
  }

  // this is a sealed trait
  def split[T](ctx: SealedTrait[T]): Typeclass[T] = (value: T) => {
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

/**
 * This trait renders case classes in key=value format, without the `@type` associated with case classes.
 */
trait KeyValueCaseClassDerivation { self: Derivation =>
  override protected def joinCaseClass[T](ctx: CaseClass[T]): Typeclass[T] = { v =>
    ToObjectValue(ctx.parameters.map { p =>
      Field.keyValue(p.label, p.typeclass.toValue(p.dereference(v)))
    })
  }
}

/**
 * This trait renders case classes in value format, without the `@type` associated with case classes.
 */
trait ValueCaseClassDerivation { self: Derivation =>
  override protected def joinCaseClass[T](ctx: CaseClass[T]): Typeclass[T] = { v =>
    ToObjectValue(ctx.parameters.map { p =>
      Field.value(p.label, p.typeclass.toValue(p.dereference(v)))
    })
  }
}
