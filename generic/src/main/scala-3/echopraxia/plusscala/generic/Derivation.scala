package echopraxia.plusscala.generic

import echopraxia.api.Field
import echopraxia.api.Value
import echopraxia.plusscala.api.*
import magnolia1.*
import magnolia1.CaseClass.Param

import scala.deriving.Mirror

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
trait AutoDerivation { self: ValueTypeClasses =>

  object ToValueAutoDerivation extends magnolia1.AutoDerivation[ToValue] {

    override def join[T](caseClass: CaseClass[ToValue, T]): ToValue[T] =
      new ToValue[T] {
        def toValue(value: T): Value[?] = {
          def parameterStrategy(parameter: Param[ToValue, T]) = {
            // What if there's a ToName defined on this guy?
            // we want toValue(value: T)(implicit ev: ToValueAttributes[T])?
            // How does play-json manage its configuration with snake_case vs camel case?
            Field.keyValue(parameter.label, parameter.typeclass.toValue(parameter.deref(value)))
          }
          val serializedParams = caseClass.parameters.map(parameterStrategy)
          ToObjectValue(serializedParams.toSeq)
        }
      }

    // generate ToValue instance for sealed traits
    override def split[T](sealedTrait: SealedTrait[ToValue, T]): ToValue[T] =
      new ToValue[T] {
        def toValue(value: T): Value[?] =
          sealedTrait.choose(value) { subtype =>
            subtype.typeclass.toValue(subtype.cast(value))
          }
      }
  }

  inline given gen[A](using Mirror.Of[A]): ToValue[A] = ToValueAutoDerivation.autoDerived
}

/**
 * Semi automatic derivation.
 */
trait SemiAutoDerivation { self: ValueTypeClasses =>

  object ToValueSemiAutoDerivation extends magnolia1.Derivation[ToValue] {

    override def join[T](caseClass: CaseClass[ToValue, T]): ToValue[T] =
      new ToValue[T] {
        def toValue(value: T): Value[?] = {
          def parameterStrategy(parameter: Param[ToValue, T]) = {
            // What if there's a ToName defined on this guy?
            // we want toValue(value: T)(implicit ev: ToValueAttributes[T])?
            // How does play-json manage its configuration with snake_case vs camel case?
            Field.keyValue(parameter.label, parameter.typeclass.toValue(parameter.deref(value)))
          }
          val serializedParams = caseClass.parameters.map(parameterStrategy)
          ToObjectValue(serializedParams.toSeq)
        }
      }

    // generate ToValue instance for sealed traits
    override def split[T](sealedTrait: SealedTrait[ToValue, T]): ToValue[T] =
      new ToValue[T] {
        def toValue(value: T): Value[?] =
          sealedTrait.choose(value) { subtype =>
            subtype.typeclass.toValue(subtype.cast(value))
          }
      }
  }

  inline def gen[A](using Mirror.Of[A]): ToValue[A] = ToValueSemiAutoDerivation.derived
}
