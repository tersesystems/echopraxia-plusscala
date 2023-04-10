
## Automatic Type Class Derivation

Mapping individual case classes and value objects can get repetitive, so there's a shortcut you can use: [type class derivation](https://blog.kaizen-solutions.io/2020/typeclass-derivation-with-magnolia/).

To add derivation to your project, include the library dependency:

```
libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "generic" % echopraxiaPlusScalaVersion
```

You can incorporate automatic type class derivation by adding the `AutoDerivation` or `SemiAutoDerivation` trait.  This trait will set up fields and values in case classes and sealed traits appropriately, using [Magnolia](https://github.com/softwaremill/magnolia/tree/scala2).

Automatic derivation applies to all case classes, while semi-automatic derivation requires the type class instance to be derived explicitly:

```scala
import com.tersesystems.echopraxia.plusscala.api._
import com.tersesystems.echopraxia.plusscala.generic._

trait AutoFieldBuilder extends FieldBuilder with AutoDerivation
object AutoFieldBuilder extends AutoFieldBuilder

trait SemiAutoFieldBuilder extends FieldBuilder with SemiAutoDerivation
object SemiAutoFieldBuilder extends SemiAutoFieldBuilder {
  // Please use `implicit lazy val` in semi-automatic derivation as a workaround
  // for https://github.com/softwaremill/magnolia/issues/402
  implicit lazy val iceCreamToValue: ToValue[IceCream] = gen[IceCream]
  implicit lazy val entityIdToValue: ToValue[EntityId] = gen[EntityId]
  implicit lazy val barToValue: ToValue[Bar] = gen[Bar]
  implicit lazy val fooToValue: ToValue[Foo] = gen[Foo]
}

final case class IceCream(name: String, numCherries: Int, inCone: Boolean)
final case class EntityId(raw: Int) extends AnyVal
final case class Bar(underlying: String) extends AnyVal
final case class Foo(bar: Bar)

object GenericMain {
  private val autoLogger = LoggerFactory.getLogger.withFieldBuilder(AutoFieldBuilder)
  private val semiAutoLogger = LoggerFactory.getLogger.withFieldBuilder(SemiAutoFieldBuilder)

  def main(args: Array[String]): Unit = {
    autoLogger.info("{}", _.keyValue("icecream", IceCream("sundae", 1, false)))
    autoLogger.info("{}", _.keyValue("entityId", EntityId(1)))
    autoLogger.info("{}", _.keyValue("foo", Foo(Bar("underlying"))))

    semiAutoLogger.info("{}", _.keyValue("icecream", IceCream("sundae", 1, false)))
    semiAutoLogger.info("{}", _.keyValue("entityId", EntityId(1)))
    semiAutoLogger.info("{}", _.keyValue("foo", Foo(Bar("underlying"))))
  }
}
```

Because automatic derivation creates fields and values automatically, decisions made about the format and structure are made by modifying the field builder.

**NOTE**: There is a natural temptation to enable automatic derivation in all cases for ease of use and dump all case classes in entirety.  This is an anti-pattern -- logging is not serialization, and unbounded logging of data structures represented by case classes can cause runtime issues.

### Type and Key Value

The default is to create a `@type` field and include it with the case class, and render all values as key value pairs.  For example,

```scala
logger.info("{}", _.keyValue("tuple", (1,2,3,4)))
```

would log the following:

```
tuple={@type=scala.Tuple4, _1=1, _2=2, _3=3, _4=4}
```

### Key Value Only

The `KeyValueCaseClassDerivation` will render only `key=value` format, without the `@type` field:

```scala
trait KeyValueOnly extends FieldBuilder with AutoDerivation with KeyValueCaseClassDerivation
object KeyValueOnly extends KeyValueOnly
logger.withFieldBuilder(KeyValueOnly).info("{}", _.keyValue("tuple", (1,2,3,4)))
```

logs the following:

```
tuple={_1=1, _2=2, _3=3, _4=4}
```

### Value Only

The `ValueCaseClassDerivation` will render only `value` format, without the `@type` field:

```scala
trait ValueOnly extends FieldBuilder with AutoDerivation with ValueCaseClassDerivation
object ValueOnly extends ValueOnly
logger.withFieldBuilder(ValueOnly).info("{}", _.keyValue("tuple", (1,2,3,4)))
```

logs the following:

```
tuple={1, 2, 3, 4}
```

### Option and Either

`Option` and `Either` are treated as regular case classes by default:

```scala
autoLogger.info("{}", _.keyValue("some", Option(1)))
autoLogger.info("{}", _.keyValue("none", None))
autoLogger.info("{}", _.keyValue("right", Right(true)))
autoLogger.info("{}", _.keyValue("left", Left(false)))
```

produces:

```
12:32:36.294 [main] INFO com.example.GenericMain$ - some={value=1}
12:32:36.295 [main] INFO com.example.GenericMain$ - none=None
12:32:36.298 [main] INFO com.example.GenericMain$ - right={value=true}
12:32:36.300 [main] INFO com.example.GenericMain$ - left={value=false}
```

To collapse `Option` and `Either` down to a direct value use `EitherValueTypes` and `OptionValueTypes`:

```scala
trait ShortFieldBuilder extends FieldBuilder with AutoDerivation with EitherValueTypes with OptionValueTypes
```

produces the following logs:

```
12:35:13.566 [main] INFO com.example.GenericMain$ - some=1
12:35:13.567 [main] INFO com.example.GenericMain$ - none=null
12:35:13.570 [main] INFO com.example.GenericMain$ - right=true
12:35:13.571 [main] INFO com.example.GenericMain$ - left=false
```

You can also use `EitherValueTypes` and `OptionValueTypes` in regular field builders for the implicits.
