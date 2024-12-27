# Scala API for Echopraxia

[Echopraxia](https://github.com/tersesystems/echopraxia) is a structured logging framework with implementations for Logback and Log4J.

The Scala API for [Echopraxia](https://github.com/tersesystems/echopraxia) is a layer over the Java API that leverages Scala features to provide type classes and source code information.  Echopraxia is compiled for Scala 2.12, 2.13, and 3.

In practical terms, you define some type classes that set what the name and value of a class should be:

```scala
trait Logging extends LoggingBase {
  implicit val uuidToField: ToField[UUID] = ToField(_ => "uuid", uuid => ToValue(uuid.toString))
}
```

and then logging will provide both structured logging in JSON and line oriented format in logfmt:

```scala
class Processor extends Logging {
  private val logger = LoggerFactory.getLogger(getClass)

  def process(): Unit {
    logger.info(UUID.randomUUID) // uses implicit type class for UUID
  }
}
```

This will print out the uuid in [logfmt](https://www.brandur.org/logfmt) when used with a pattern format appender:

```
16:45:32.905 INFO  [main]: uuid=9e6805df-a211-4129-b96d-882e0d9eb609
```

and will print out JSON when using a structured logging appender like [logstash-logback-appender](https://github.com/logfellow/logstash-logback-encoder) or [Log4J2 template layout format](https://logging.apache.org/log4j/2.x/manual/json-template-layout.html):

```json
{
  "@timestamp": "...",
  "level": "INFO",
  "uuid": "9e6805df-a211-4129-b96d-882e0d9eb609"
}
```

## Examples

For the fastest possible way to try out Echopraxia, download and run the [Scala CLI script](https://github.com/tersesystems/smallest-dynamic-logging-example/blob/main/scala-cli/script.sc).

Examples are available at [tersesystems/echopraxia-scala-example](https://github.com/tersesystems/echopraxia-scala-example) and [tersesystems/echopraxia-examples](https://github.com/tersesystems/echopraxia-examples).

## What Does It Mean?

Q: What does this mean for developers?

A: You can write code faster and with fewer bugs by using Echopraxia for debugging.  Echopraxia is oriented for "printf debugging", so all the `println` and `toString` methods that go into your code at development can be entered as logging statements.  You can [snapshot](https://www.scalactic.org/user_guide/Snapshots) and dump your internal state easily.  You can also easily disable or filter debug logging using conditions.

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
import echopraxia.plusscala._
import echopraxia.plusscala.api.LoggingBase

trait Logging extends LoggingBase {
  // add implicits for your classes here
}

object CustomFieldBuilder extends PresentationFieldBuilder with Logging

class Example extends Logging {
  val logger = LoggerFactory.getLogger(CustomFieldBuilder)

  def doStuff: Unit = {
    logger.info("do some stuff")
  }
}
```

The logger takes `Field*` as arguments, and `LoggingBase` contains the implicit conversions to turn tuples into fields:

```scala
// val field: echopraxia.api.Field = "name" -> "will" under the hood
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

## Field Builders

There are some values that are awkward to represent using implicit conversion, such as when you want to render a `null` explicitly.  The logger comes with a field builder function that can be used for fine-grained control of arguments.  In this case, we can use `nullField` to render the field.

```scala
logger.debug("this will render foo=null -- {}", _.nullField("foo"))
```

Field builders can take custom methods.  This is particularly when you want to add complex field conversion logic inside a logging statement that you don't want available to the enclosing class, i.e.

```scala
object MyFieldBuilder extends PresentationFieldBuilder with Logging {
  def state1(foo: Foo): Field = keyValue(foo, foo).withDisplayName("foo in state one")
}

val logger = LoggerFactory.getLogger(getClass, MyFieldBuilder)

logger.info("User {} can do complex method {}", fb.list(
  fb.keyValue("name" -> "will"),
  fb.state1(foo)
))
```

You can append fields together using `++` from `LowPriorityImplicits` if you prefer to not use `fb.list`:

```scala
import echopraxia.api._
logger.info("User {} can do complex method {}", fb => fb.keyValue("name" -> "will") ++ fb.myComplexMethod(foo))
```

Field builder functions have the advantage of being lazily evaluated, which makes them useful in a debugging context.  For example, if you are rendering a field for debugging, you may want to wrap it in a conditional to avoid unnecessary object creation:

```scala
if (logger.isLoggingDebug()) {
  val field: Field = "foo" -> foo
  logger.debug("use isLoggingDebug to avoid creation of field {}", field)
}
```

This is not needed with field builder functions, as they are only executed when the log level is met.

```scala
logger.debug("This only creates field if logger.level >= DEBUG {}", _.keyValue("foo" -> foo"))
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

Extending logging is done by extending the `ToValue`, `ToName` type classes that are packaged in `LoggingBase`.

### ToValue

The `ToValue` type class looks like this:

```scala
package echopraxia.plusscala.api
import echopraxia.api.Value

trait ValueTypeClasses {
  trait ToValue[-T] {
    def toValue(t: T): Value[_]
  }
}
```

Let's start off by adding a `ToValue` for `java.time.Instant`, by converting it to a string.  There are already `ToValue` mappings for all the built-ins like string, boolean, and numbers, so calling `ToValue(instant.string)` will resolve the implicit and return a `Value[String]`.

```scala
import echopraxia.plusscala.api._
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
  implicit def tupleToValue[TVK: ToValue, TVV: ToValue]: ToValue[Tuple2[TVK, TVV]] = { case (k, v) =>
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

Rather than using a tuple, you can specify a default name for a field using the `ToName` value class.

The `ToName` type class looks like this:

```scala
trait NameTypeClass {
  trait ToName[-T] {
    def toName(t: Option[T]): String
  }
}
```

and is typically defined as an implicit like this:

```scala
trait Logging extends LoggingBase {
  implicit val instantToName: ToName[Instant] = _ => "instant"
}
```

There are traits that provide implicits that can resolve common effects, such as `OptionToNameImplicits`, `TryToNameImplicits`, and `EitherToNameImplicits`.

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

## Field Presentation

There are times when the default field presentation is awkward, and you'd like to cut down on the amount of information displayed in the message. You can do this by adding presentation hints to the field.

### AsValueOnly

The `asValueOnly` method has the effect of turning a "key=value" field into a "value" field in text format, just like the value method:

```scala
val field: Field = "name" -> "value"
field.asValueOnly.toString must be("value")
```

### Display Name

The `withDisplayName` method shows a human-readable string in text format bracketed in quotes:

```scala
val field: Field = "name" -> 1
field.withDisplayName("human readable name").toString must be("\"human readable name\"=1")
```

### Elided

The `asElided` method will elide the field so that it is passed over and does not show in text format:

```scala
val field: Field = "name" -> 1
field.asElided.toString must be("")
```

## Value Presentation

Value presentation changes how values are rendered in a line oriented format, so that they are more human readable. Value presentation is different from field presentation in that the field name cannot be changed in value presentation.

### AsCardinal

The asCardinal method, when used on an array value or on a string value, displays the number of elements in the array bracketed by "|" characters in text format:

```scala
val cardinalField: Field = "elements" -> ToValue(1,2,3).asArray.asCardinal();
cardinalField.toString(); // renders elements=|3|
```

or for a string value:

```scala
val cardinalField: Field = "elements" -> ToValue("123").asString.asCardinal();
cardinalField.toString(); // renders elements=|3|
```

### AbbreviateAfter

The `abbreviateAfter` method will truncate an array or string that is very long and replace the rest with ellipsis:

```scala
var abbrField = keyValue("abbreviatedField", ToValue(veryLongString).asString.abbreviateAfter(5));
abbrField.toString(); // renders abbreviatedField=12345...
```

### ToStringValue

The `withToStringValue` uses a custom string for the value, providing something more human-readable. This is particularly useful in arrays and complex nested objects, where you may want a summary of the object rather than the full JSON rendering.

```scala
val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault());
val instantField: Field = "instant" -> ToValue(instant.toString()).withToStringValue(formatter.format(instant));
instantField.toString(); // renders ISO8601 in JSON, but 01/01/1970 with toString()
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
import echopraxia.plusscala.api._
```

or

```scala
import echopraxia.plusscala.api.Implicits._
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
  logger.info(condition, "person matches! {}", "person" -> thisPerson)
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
