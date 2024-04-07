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
// check github releases or mvnrepository.com for latest
libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "logger" % echopraxiaPlusScalaVersion
```

and one of the underlying core logger providers and frameworks, i.e. for `logstash-logback-encoder`:

```scala
libraryDependencies += "com.tersesystems.echopraxia" % "logstash" % "3.1.2" // provides core logger
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.5.3" // logback 1.2, 1.3, or 1.4 are supported
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
import com.tersesystems.echopraxia.plusscala.api.LoggingBase

trait Logging extends LoggingBase

class Example extends Logging {
  val logger = LoggerFactory.getLogger

  def doStuff: Unit = {
    logger.info("do some stuff")
  }
}
```

The logger takes `Field*` as arguments, and `LoggingBase` contains the implicit conversions to turn tuples into fields:

```scala
// val field: com.tersesystems.echopraxia.api.Field = "name" -> "will" under the hood
logger.info("hi {}", "name" -> "will") // prints hi name=will
```

You can either provide an explicit message, or just provide the fields themselves for shortcut.

```scala
logger.info("name" -> "will", "admin" -> true)
// same as logger.info("{} {}", "name" -> "will", "admin" -> true)
```

You can also handle lists:

```scala
val seq = Seq("first" -> 1, "second" -> 2)
logger.info("seq = {}", "listOfTuples" -> seq)
```

There is a slight problem when you render lists which do not have a common base type.  In this case you have to explicitly add `HeterogeneousFieldSupport` in, which will map iterables as arrays automatically, and then cast to `Seq[Field]`:

```scala
trait Logging extends LoggingBase with HeterogeneousFieldSupport

logger.info("list" -> Seq[Field]("name" -> "will", "admin" -> true))
```

You can, of course, log exceptions automatically, either with or without a message.

```scala
logger.error("something went wrong: {}", e)
// this also works
logger.error(e)
```

### Options, Either, and Future

Commonly used types like `Option`, `Either`, and `Future` are handled automatically by `LoggingBase`:

```scala
val optInt: Option[Int] = None
logger.info("optInt" -> optInt)  // None will render as "optInt": null in JSON.
```

or for Either:

```scala
val eitherInt: Either[Int, String] = Left(1)
logger.info("eitherInt" -> eitherInt) // "eitherInt": 1
```

or for futures, you get an object back that has the completed status and if completed, either the success or failure value.

```scala
val future = Future.successful("yay")
logger.info("future" -> future) // future={completed=true, success=yay}
```

## Extending Logging

Extending logging is done by extending the `ToValue`, `ToName`, and `ToValueAttributes` type classes that are packaged in `LoggingBase`.

### ToValue

The `ToValue` type class looks like this:

```scala
package com.tersesystems.echopraxia.plusscala.api
import com.tersesystems.echopraxia.api.Value

trait ValueTypeClasses {
  trait ToValue[-T] {
    def toValue(t: T): Value[_]
  }
}
```

Let's start off by adding a `ToValue` for `java.time.Instant`, by converting it to a string.  There are already `ToValue` mappings for all the built-ins like string, boolean, and numbers, so calling `ToValue(instant.string)` will resolve the implicit and return a `Value[String]`.

```scala
import com.tersesystems.echopraxia.plusscala.api.LoggingBase
import java.time.Instant

trait Logging extends LoggingBase {
  implicit val instantToValue: ToValue[Instant] = instant => ToValue(instant.toString)
}
```

Now you can log `Instant` in the same way:

```scala
logger.info("now" -> Instant.now)
```

If you want to render an object, you must use `ToObjectValue`, which takes a `Seq[Field]`:

```scala
case class Person(name: String, age: Int)

implicit val personToValue: ToObjectValue[Person] = { person =>
  ToObjectValue(
    "name" -> person.name,
    "age" -> person.age
  )
}
```

You can also specify a common format for tuples and maps:

```scala
trait Logging extends LoggingBase {
  implicit def tupleToValue[TVK: ToValue: ToValueAttributes, TVV: ToValue: ToValueAttributes]: ToValue[Tuple2[TVK, TVV]] = { case (k, v) =>
    ToObjectValue("key" -> k, "value" -> v)
  }
}
```

allows for rendering of a map as a series of tuples:

```scala
// people=[{key=person1, value={name=Person1, age=12}}, {key=person2, value={name=Person2, age=15}}]
logger.info("people" -> Map("person1" -> person1, "person2" -> person2))
```

You can also include more complex logic in a `ToValue`, for example dealing with sensitive values can be handled by adding an implicit flag:

```scala
trait Logging extends LoggingBase {
  implicit def creditCardToValue(implicit cap: Sensitive = Censored): ToValue[CreditCard] = cc => {
    ToObjectValue(
      sensitiveKeyValue("cc_number", cc.number),
      "expiration_date" -> cc.expirationDate
    )
  }

  def sensitiveKeyValue(name: String, value: String)(implicit cap: Sensitive = Censored): Field = {
    cap match {
      case Censored =>
        name -> "[CENSORED]"
      case Explicit =>
        name -> value
    }
  }

  sealed trait Sensitive
  case object Censored extends Sensitive
  case object Explicit extends Sensitive
}
```

### ToName

Rather than using a tuple, you can specify a default name using `ToName`.

The `ToName` type class looks like this:

```scala
trait ToName[-T] {
  def toName(t: T): String
}
```

and is defined as follows

```scala
trait Logging extends LoggingBase {
  implicit val instantToName: ToName[Instant] = _ => "instant"
}
```

If a type has both `ToName` and `ToValue` specified on it, then you can pass in the object and have a field rendered automatically.

```scala
val epoch = Instant.EPOCH
logger.info(epoch) // instant=1970-01-01T00:00:00Z
```

This comes in very handy when using `Future`, for example:

```scala
trait Logging extends LoggingBase {
  implicit def futureToName[T: ClassTag]: ToName[Future[T]] = _ => s"future[${classTag[T].runtimeClass.getName}]"
}
```

yields the future's type:

```scala
logger.info(Future.successful(true)) // future[boolean]={completed=true, success=true}
```

### ToField

Because `ToName` and `ToValue` are commonly specified together, you can set mappings for both at the same time using `ToField`.

For example, rather than defining

```scala
case class Title(raw: String)    extends AnyVal

trait Logging extends LoggingBase {
  implicit val titleToName: ToName[Title] = _ => "title"
  implicit val titleToValue: ToValue[Title] = t => ToValue(t.raw)
}
```

You could define both with `ToField(toNameFunction, toFieldFunction)`:

```scala
trait Logging extends LoggingBase {
  implicit val titleToField: ToField[Title] = ToField(_ => "title", t => ToValue(t.raw))
}
```

This gets especially useful when you are building up complex state objects where the case class fields all line up:

```scala
trait Logging extends LoggingBase {
  implicit val titleToField: ToField[Title] = ToField(_ => "title", t => ToValue(t.raw))

  implicit val authorToField: ToField[Author] = ToField(_ => "author", a => ToValue(a.raw))

  implicit val categoryToField: ToField[Category] = ToField(_ => "category", c => ToValue(c.raw))

  implicit val bookToField: ToField[Book] = ToField(_ => "book", book => ToObjectValue(book.title, book.category, book.author, book.price))
}
```

yields

```scala
val book1 = Book(
  Category("reference"),
  Author("Nigel Rees"),
  Title("Sayings of the Century")
)
logger.info(book) // book={title=Sayings of the Century, category=reference, author=Nigel Rees}
```

## ToValueAttributes

Echopraxia logs in both logfmt and in JSON.  In most cases this works pretty well, but there are some cases where JSON can be excessively verbose and not useful for line oriented debugging.  The `ToValueAttributes` type class handles these cases by customizing the presentation logic.

### ToStringFormat

The `toStringFormat` attribute will rewrite the output for line oriented format.

Let's say you have a `Price` that consists of an amount and a currency.  You want to render the price as `price=$8.95` in oriented format, instead of every element of the case class.

```scala
trait Logging extends LoggingBase {
  implicit val priceToField: ToField[Price] = ToField(_ => "price", price => ToObjectValue(price.currency, "amount" -> price.amount))

  implicit val priceToStringFormat: ToStringFormat[Price] = (price: Price) => {
    import java.text.NumberFormat
    val numberFormat = NumberFormat.getCurrencyInstance
    numberFormat.setCurrency(price.currency)
    ToValue(numberFormat.format(price.amount))
  }
}
```

### AbbreviateAfter

The `abbreviateAfter` attribute abbreviates a string or array so that elements after the given maximum are not shown.

```scala
  describe("AbbreviateAfter") {
    it("should abbreviate a string") {
      implicit val abbreviateUUID: AbbreviateAfter[UUID] = AbbreviateAfter(4)
      implicit val uuidToValue: ToValue[UUID]            = uuid => ToValue(uuid.toString)

      val field: Field = "uuid" -> UUID.fromString("eb1497ad-e3c1-45a3-8305-9d394a72afbe")
      field.toString must be("uuid=eb14...")
    }

    it("should abbreviate an array") {
      implicit val abbreviateUUID: AbbreviateAfter[Seq[Int]] = AbbreviateAfter(4)

      val field: Field = "array" -> Seq(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
      field.toString must be("array=[1, 2, 3, 4...]")
    }
  }
```

### Elided

The `elided` attribute elides a given field from being seen in line oriented format.

```scala
  describe("Elided") {
    it("should elide a field") {
      implicit val payloadAsElided: Elided[Payload]   = Elided[Payload]
      implicit val emailToField: ToField[Email]       = ToField[Email](_ => "email", c => ToValue(c.value))
      implicit val payloadToField: ToField[Payload]   = ToField[Payload](_ => "payload", c => ToValue(Base64.getEncoder.encodeToString(c.value)))
      implicit val downloadToField: ToField[Download] = ToField[Download](_ => "download", c => ToObjectValue(c.email, c.payload))

      val download     = Download(new Email("user@example.org"), new Payload(Array.emptyByteArray))
      val field: Field = "download" -> download
      field.toString must be("download={email=user@example.org}")
    }
  }
```

### AsCardinal

The `AsCardinal` attribute displays a string or array as the cardinal (amount) of elements.

```scala
  describe("AsCardinal") {
    it("should cardinal an array of bytes") {
      implicit val payloadAsCardinal: AsCardinal[Payload] = AsCardinal[Payload]
      implicit val emailToField: ToField[Email]           = ToField[Email](_ => "email", c => ToValue(c.value))
      implicit val payloadToField: ToField[Payload]       = ToField[Payload](_ => "payload", c => ToValue(Base64.getEncoder.encodeToString(c.value)))
      implicit val downloadToField: ToField[Download]     = ToField[Download](_ => "download", c => ToObjectValue(c.email, c.payload))

      val download     = Download(new Email("user@example.org"), new Payload(Array.emptyByteArray))
      val field: Field = "download" -> download
      field.toString must be("download={email=user@example.org, payload=|0|}")
    }
  }
```

### AsValueOnly

The `AsValueOnly` attribute will show only the field's value and not the field's name.

```scala
  describe("AsValueOnly") {
    it("should render only the value") {
      implicit val uuidAsValueOnly: AsValueOnly[UUID] = AsValueOnly[UUID]
      implicit val uuidToValue: ToValue[UUID]         = uuid => ToValue(uuid.toString)

      val field: Field = "uuid" -> UUID.fromString("eb1497ad-e3c1-45a3-8305-9d394a72afbe")
      field.toString must be("eb1497ad-e3c1-45a3-8305-9d394a72afbe")
    }

    it("should work with arrays") {
      implicit val intAsValueOnly: AsValueOnly[Int] = AsValueOnly[Int]
      val field = ("tuple" -> Seq(1, 2))
      field.toString must be("[1, 2]")
    }
  }
```

### Display Name

The `WithDisplayName` attribute will display a human readable name instead of the field name.

```scala
  describe("WithDisplayName") {
    it("should render with a display name") {
      implicit val uuidDisplayName: WithDisplayName[UUID] = WithDisplayName[UUID]("unique id")
      implicit val uuidToValue: ToValue[UUID]             = uuid => ToValue(uuid.toString)

      val field: Field = "uuid" -> UUID.fromString("eb1497ad-e3c1-45a3-8305-9d394a72afbe")
      field.toString must be("\"unique id\"=eb1497ad-e3c1-45a3-8305-9d394a72afbe")
    }
  }
```

## Context

You can compose loggers with [context](https://github.com/tersesystems/echopraxia#context) using `withFields` and the context fields will render in JSON:

```scala
val loggerWithField = logger.withFields("correlationId" -> correlationId)
loggerWithField.info("renders with correlationId in JSON")
```

This is "call by name" i.e. the function defined is evaluated on every logging statement and may change between logging statements.

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
private val willField: Field = ("person" -> Person("will", 1))
private val condition: Condition = Condition(_.fields.contains(willField))

def conditionUsingFields() = {
  val thisPerson = Person("will", 1)
  logger.info(condition, "person matches! {}", ("person" -> thisPerson))
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
