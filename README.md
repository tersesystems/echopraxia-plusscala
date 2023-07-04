# Scala API for Echopraxia

[Echopraxia](https://github.com/tersesystems/echopraxia) is a structured logging framework with implementations for Logback and Log4J.  The Scala API for [Echopraxia](https://github.com/tersesystems/echopraxia) is a layer over the Java API that works smoothly with Scala types and has a number of features to make debugging even smoother, including a "trace" logger and automatic type class derivation.  

Echopraxia is compiled for Scala 2.12 and Scala 2.13.  

## Examples

For the fastest possible way to try out Echopraxia, download and run the [Scala CLI script](https://github.com/tersesystems/smallest-dynamic-logging-example/).

Examples are available at [tersesystems/echopraxia-scala-example](https://github.com/tersesystems/echopraxia-scala-example) and [tersesystems/echopraxia-examples](https://github.com/tersesystems/echopraxia-examples).

## What Does It Mean?

Q: What does this mean for developers?

A: You can write code faster and with fewer bugs by using Echopraxia for debugging.  Echopraxia is oriented for "printf debugging", so all the `println` and `toString` methods that go into your code at development can be entered as logging statements.  Because logging statements are functions, they're only executed when they meet logging conditions, so the cost is low.   And because you can define fields using custom field builders, you can dump your internal state easily.  You can also easily disable a logger completely by adding `Condition.never`, which will switch it to a `no-op` statement.

Q: What does this mean for operations?

A: Echopraxia makes your application more observable through structured logging and automatic "call-by-name" methods that can capture request and trace contexts.  Echopraxia also targets managing logging on a budget -- determining "when to log" on an already deployed application.  All logging statements in Echopraxia are based around fields and values, and can incorporate complex conditional logic that can alter [logging at runtime](https://github.com/tersesystems/dynamic-debug-logging), down to individual statements.

Q: Is this a replacement for SLF4J / Log4J / Logback?

A: You can use Echopraxia and your usual logging framework side by side in the same application.  Echopraxia sends correctly formatted structured logging messages to the underlying framework and is well-behaved, tracking the implementations enabled logging levels and filters.

## Logger

Add the following to your `build.sbt` file:

```scala
libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "logger" % echopraxiaPlusScalaVersion
```

and one of the underlying core logger providers and frameworks, i.e. for `logstash-logback-encoder`:

```scala
libraryDependencies += "com.tersesystems.echopraxia" % "logstash" % "3.0.2" // provides core logger
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.4.8" // logback 1.2, 1.3, or 1.4 are supported
libraryDependencies += "net.logstash.logback" % "logstash-logback-encoder" % "7.4"
```

or for log4j2:

```scala
libraryDependencies += "com.tersesystems.echopraxia" % "log4j" % "3.0.2"
libraryDependencies += "org.apache.logging.log4j" % "log4j-core" % "2.20.0"
libraryDependencies += "org.apache.logging.log4j" % "log4j-layout-template-json" % "2.20.0"
```

To import the Scala API, add the following:

```scala
import com.tersesystems.echopraxia.plusscala._

class Example {
  val logger = LoggerFactory.getLogger
  
  def doStuff: Unit = {
    logger.info("do some stuff")
  }
}
```

The Scala logger (technically, the default field builder) has some enhancements over the Java logger, notably the ability to add fields that have a name and value.  

```scala
logger.info("hi {}", _.string("name", "will")) // two args
logger.info("hi {}", _.string("name" -> "will")) // tuple
```

If you want to add several fields, you can use `fb.list` to return the fields:

```scala
logger.info("{} {}", fb => fb.list(
  fb.string("name" -> "will"),
  fb.bool("admin" -> true)
))
```

You can map sequences of tuples into arguments by mapping them:

```scala
val seq = Seq("first" -> 1, "second" -> 2)
logger.info("seq = {}", fb => fb.list(seq.map(fb.number)))
```

And you can log exceptions, which will render stacktraces and also as toString:

```scala
logger.error("something went wrong: {} threw {}", fb.list(
  fb.keyValue("messageId" -> messageId),
  fb.exception(e)
))
```

You can also compose loggers with [context](https://github.com/tersesystems/echopraxia#context) using `withFields` and the context fields will render in JSON:

```scala
val loggerWithField = logger.withFields(fb => fb.keyValue("correlationId" -> correlationId))
loggerWithField.info("renders with correlationId in JSON")
```

This is "call by name" i.e. the function defined is evaluated on every logging statement and may change between logging statements.

Composition works like you'd expect with fields and conditions (see the Conditions section), but field building is the important bit so we'll go through that first.

## Field Builder

A `Field` is defined as a `name: String` and a `value: com.tersesystems.echopraxia.api.Value`.  The field builder has methods to create fields. 

* `fb.keyValue`: renders a field with `name=value` when rendered in logfmt line oriented text.
* `fb.value`: renders a field with `value` when rendered in logfmt line oriented text.

The out-of-the-box field builder comes with some additional methods for common types, i.e.

* `fb.string`: creates a field with a string as a value, same as `fb.keyValue(name, Value.string(str))`.
* `fb.number`: creates a field with a number as a value, same as `fb.keyValue(name, Value.number(num))`.
* `fb.bool`: creates a field with a boolean as a value, same as `fb.keyValue(name, Value.bool(b))`.
* `fb.nullValue`: creates a field with a null as a value, same as `fb.keyValue(name, Value.nullValue())`
* `fb.array`: creates a field with an array as a value, same as `fb.keyValue(name, Value.array(arr))`
* `fb.obj`: creates a field with an object as a value, same as `fb.keyValue(name, Value.``object``(o))`
* `fb.exception`: renders exception field and sets throwable on logging event.

When rendering using a line oriented encoder, `fb.array` and `fb.obj` render in logfmt style `key=value` format, and the other methods use the `value` format.

```scala
import com.tersesystems.echopraxia.plusscala._
import com.tersesystems.echopraxia.plusscala.api._

class Example {
  val logger = LoggerFactory.getLogger

  def doStuff: Unit = {
    logger.info("{} {} {} {}", fb => {
      import fb._
      obj("person" -> 
        Seq(
          keyValue("number" -> 1),
          keyValue("bool" -> true),
          keyValue("ints" -> Seq(1, 2, 3)),
          keyValue("strName" -> "bar")
        )
      )
    })
  }
}
```

Arrays will take a `Seq` of values, including object values.  Object values take a sequence of fields as arguments, and can be defined using the `ToObjectValue(fields)` method. For example, the first element in the [path example from Json-Path](https://github.com/json-path/JsonPath#path-examples) can be represented as:

```scala
logger.info("{}", fb => {
  import fb._
  fb.obj("store" ->
    fb.array("book" -> Seq(
      ToObjectValue(
        fb.keyValue("category", "reference"),
        fb.keyValue("author", "Nigel Rees"),
        fb.keyValue("title", "Sayings of the Century"),
        fb.keyValue("price", 8.95)
      )
    ))
  )
})
```

Producing the following in line oriented formats:

```
store={book=[{category=reference, author=Nigel Rees, title=Sayings of the Century, price=8.95}]}
```

## Custom Field Builder

Although using the default field builder is great for one-offs, if you want to log more complex objects it can be tedious to render a large object down to its component parts.  To make it easier, Echopraxia incorporates custom fields builders that can be domain specific, and allows you to stack them together.

You can create your own field builder and define type class instances, using `ToValue` and `ToObjectValue`.  You can log arbitrary classes `Foo` and `Bar`:

```scala
import com.tersesystems.echopraxia.plusscala.LoggerFactory
import com.tersesystems.echopraxia.plusscala.api.FieldBuilder

object Main {
  // Logger[MyFieldBuilder.type] can use implicits w/o import tax  
  private val logger = LoggerFactory.getLogger(MyFieldBuilder)

  def main(args: Array[String]): Unit = {
    logger.debug("Hello world!")

    val foo = Foo("name", 1)
    val bar = Bar(true, 0x1)

    logger.debug("{}", fb => fb.keyValue("foo", foo))
    logger.debug("{}", fb => fb.keyValue("bar", bar))
  }
}

case class Foo(name: String, age: Int)

case class Bar(herp: Boolean, derp: Byte)

trait FooBuilder extends FieldBuilder {
  implicit val fooToObjectValue: ToObjectValue[Foo] = { value =>
    ToObjectValue(
      keyValue("name", value.name),
      keyValue("age", value.age)
    )
  }
}

trait BarBuilder extends FieldBuilder {
  implicit val barToObjectValue: ToObjectValue[Bar] = { bar =>
    ToObjectValue(
      keyValue("herp", bar.herp),
      keyValue("derp", bar.derp)
    )
  }
}

trait MyFieldBuilder extends FooBuilder with BarBuilder

object MyFieldBuilder extends MyFieldBuilder
```

Note that the implicits will only be visible in the singleton object scope, so you must have an `object` handy.  If you want to add some one-off methods or functions, it's easiest to create a one-time object and use `withFieldBuilder`:

```scala
object Main {

  object SomeObjectBuilder extends MyFieldBuilder {    
    val function: (Foo, Bar) => FieldBuilderResult = { case (foo, bar) =>
      list(
        obj("foo", foo),
        obj("bar", bar)
      )
    }

    def method(foo: Foo, bar: Bar): FieldBuilderResult = {
      list(
        obj("foo", foo),
        obj("bar", bar)
      )
    }
  }

  private val logger = LoggerFactory.getLogger(SomeObjectBuilder)

  def main(args: Array[String]): Unit = {
    logger.debug("Hello world!")

    val foo = Foo("name", 1)
    val bar = Bar(true, 0x1)
    
    logger.debug("using method {} {}", _.method(foo, bar))
    logger.debug("using function {} {}", _.function(foo, bar))
  }
}
```

You should only have one type class instance for the type you are handling.  If you are interested in subclassing objects in different modules, you can call the `super` value of the parent field builder.  

For example, imagine you have an `animal` module that contains the `Animal` class:

```scala
class Animal(val name: String, val color: String)

trait AnimalFieldBuilder extends FieldBuilder {
  // this must be a method so we can call super
  implicit def animalToValue: ToValue[Animal] = { a =>
    ToObjectValue(keyValue("name" -> a.name), keyValue("color" -> a.color))
  }
}
```

And in another module, we want to subclass `Animal` and leverage the field builder logic from `AnimalFieldBuilder`:

```scala
class Cat(name: String, color: String, val goodCat: Boolean) extends Animal(name, color)

// Special case cat as an animal by overriding animalToValue
trait CatFieldBuilder extends AnimalFieldBuilder {
  override implicit val animalToValue: ToValue[Animal] = { animal =>
    // enrich Value[_]
    import com.tersesystems.echopraxia.plusscala.api.Implicits._
    // call super method and cast to ObjectValue
    val animalValue = super.animalToValue.toValue(animal).asObject
    animal match {
      case cat: Cat =>
        // Add fields to object value
        animalValue.add(keyValue("goodCat" -> cat.isGoodCat))
      case _ =>
        animalValue
    }
  }
}
object CatFieldBuilder extends CatFieldBuilder
```

Now we can call the `ToValue[Animal]` in `CatFieldBuilder` and it will contain the `goodCat` field.

```scala
val fb = CatFieldBuilder
val cat = new Cat("indra", "black", goodCat = true)
val field = fb.keyValue("cat", cat)
```

You can extend the field builder for more general purposes, for example to treat all `Map[String, V]` as objects:

```scala
trait MapFieldBuilder extends FieldBuilder {
  implicit def mapToObjectValue[V: ToValue]: ToObjectValue[Map[String, V]] = 
    m => ToObjectValue(m.map(keyValue))
}
```

You can also create fields outside the context of a logging statement, and append or prepend them as necessary, by using the singleton factory directly, and using the FieldBuilderResult `concat` or `++` operator:

```scala
import com.tersesystems.echopraxia.plusscala.api._ // for `++` append operation

val fields: FieldBuilderResult = MyFieldBuilder.keyValue("foo" -> foo)
logger.info("external {} {}", fb => fields ++ fb.keyValue("true" -> true))
```

## API

You can convert levels, conditions, and logging contexts to Java using the `.asJava` suffix.

Conversion of Java levels, conditions, and logging contexts are handled through type enrichment adding `.asScala` methods to the classes.

To enable type enrichment, import the `api` package,

```scala
import com.tersesystems.echopraxia.plusscala.api._
```

or 

```scala
import com.tersesystems.echopraxia.plusscala.api.Implicits._
```

explicitly if you only want the implicits.

This is useful when using the [condition scripts](https://github.com/tersesystems/echopraxia#dynamic-conditions-with-scripts) module of Echopraxia, for example.

## Conditions

Conditions in the Scala API use Scala idioms and classes.  The `find` methods in the logging context are converted to Scala, so `java.math.BigInteger` is converted to `BigInt`, for example:

```scala
val bigIntCondition = Condition(_.findNumber("$.bigInt").contains(BigInt("52")))
val bigIntLogger = logger.withCondition(bigIntCondition)
bigIntLogger.info("only logs if bigInt is 52", _.number("bigInt", BigInt("52")))
```

Likewise, if you look up `findList` to find an object, it will return the object as a `Map[String, Any]` which you can then match on.

```scala
val isWill = Condition { (context: LoggingContext) =>
  val list = context.findList("$.person[?(@.name == 'will')]")
  val map = list.head.asInstanceOf[Map[String, Any]]
  map("name") == "will"
}
```

Also, `ctx.fields` returns a `Seq[Field]` which allows you to match fields using the Scala collections API.  You can use this to match on fields and values without using a JSON path, which can be useful when you want to match on an entire object rather than a single path.

```scala
// matching a field is easier than multiple inline predicates
private val willField: Field = MyFieldBuilder.person("person", Person("will", 1))
private val condition: Condition = Condition(_.fields.contains(willField))

def conditionUsingFields() = {
  val thisPerson = Person("will", 1)
  logger.info(condition, "person matches! {}", _.person("person" -> thisPerson))
}
```

Levels in conditions have in-fix comparison operators:

```scala
val infoOrHigherCondition: Condition = Condition { (level, ctx) =>
  level >= Level.INFO // same as greaterThanOrEqual
}
```

Conditions can be composed using the logical operators `and`, `or`, and `xor`:

```scala
val andCondition: Condition = conditionOne and conditionTwo
val orCondition: Condition = conditionOne or conditionTwo
val xorCondition: Condition = conditionOne xor conditionTwo
```

There are two special conditions, `Condition.always` and `Condition.never`.  Using one of these conditions will short-circuit other conditions under the right circumstances, and can enable logging optimizations.

For example, using `logger.withCondition(Condition.always)` will return the same logger, while using `Condition.never` will result in a no-op logger being returned:

```scala
val neverLogger = logger.withCondition(Condition.never)
neverLogger.error("I will never log") // no-op
```

Because the JVM is very good at optimizing out no-op methods, using `Condition.never` is only ~1ns overhead over a straight call.

You can change these constants to have different names by overriding the resource bundle.  You can also override the `sourceInfoFields` using a custom logger to change or suppress source code fields entirely.

## Further Reading

There are more features available in Echopraxia that demonstrate what you can do with loggers:

* [Custom Logger](docs/CUSTOM.md)
* [Flow Logger](docs/FLOW_LOGGER.md)
* [Trace Logger with Compile Time Source Code Info](docs/TRACE_LOGGER.md)

You can also change the way field builders work so fields and values are automatically generated by macros or diffs between fields:

* [NameOf Field Builder](docs/NAMEOF.md)
* [Diff Field Builder](docs/DIFF_FIELD_BUILDER.md)
* [Case Classes Field Builders with Macro Derivation](docs/MACRO_DERIVED_LOGGING.md)
