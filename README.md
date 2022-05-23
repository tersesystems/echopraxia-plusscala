# Scala API for Echopraxia

The Scala API for [Echopraxia](https://github.com/tersesystems/echopraxia) is a layer over the Java API that works smoothly with Scala types.  It is compiled for Scala 2.12 and Scala 2.13.

## Logger

Add the following to your `build.sbt` file:

```scala
libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "logger" % echopraxiaPlusScalaVersion
```

and one of the underlying core logger providers, i.e.

```scala
// uncomment only one of these
// libraryDependencies += "com.tersesystems.echopraxia" % "logstash" % "2.0.1"
// libraryDependencies += "com.tersesystems.echopraxia" % "log4j" % "2.0.1"
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

The Scala logger has some enhancements over the Java logger, notably the ability to add tuples.  

```scala
logger.info("hi {}", _.string("name", "will")) // two args
logger.info("hi {}", _.string("name" -> "will")) // tuple
```

This can come in very handy when using collections of tuples, because you can map them directly to fields:

```scala
val seq = Seq("first" -> 1, "second" -> 2)
logger.info("seq = {}", fb => fb.list(seq.map(fb.number)))
```

## Async Logger

There is an asynchronous logger variant which has the same API, but logs messages asynchronously.  By default, this uses `ForkJoin.commonPool()`.

```scala
libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "async" % echopraxiaPlusScalaVersion
```

You can create an async logger using the `AsyncLoggerFactory`:

```scala
import com.tersesystems.echopraxia.plusscala.async.AsyncLoggerFactory

val asyncLogger = AsyncLoggerFactory.getLogger
asyncLogger.info("async message {}", _.string("name" -> "value"))
```

And it will produce a message showing the ForkJoinPool as the calling thread:

```
22:53:27.415 [ForkJoinPool.commonPool-worker-19] INFO com.example.Main$ - async message value
```

Because conditional logging takes place in a different thread and can be dynamic, async logging is not compatible with the `if (logger.isDebugEnabled())` style of logging, especially when an expensive operations or thread local condition are being evaluated:

```scala
val asyncLogger = AsyncLoggerFactory.getLogger.withCondition(expensiveCondition)

if (asyncLogger.isDebugEnabled()) { // evaluate condition in this thread?
  // put expensive debug statement together in this thread or other thread?
  val expensiveResult = expensiveDebugLoggingQuery()
  // evaluate condition again?  Is condition still valid?
  asyncLogger.debug("expensive but worth it {}", _.string("result", expensiveResult))
}
```

To account for this, async logging handles conditions and expensive queries by returning a handle that ensures all computation happens in the logging thread.  (This is a little different from the Java API, because IntelliJ's Scala mode will get confused by overloaded methods and parameterized types.)

```scala
asyncLogger.ifDebugEnabled { log => // condition evaluation
  val result = expensiveDebugLoggingQuery() // queries in logging thread
  log("async expensive result {}", _.string("result", result)) // handle does not evaluate
}
```

## Field Builder

A `Field` is defined as a `name` and a `value`.  The field builder has methods to create

* `fb.string`: creates a field with a string as a value
* `fb.number`: creates a field with a number as a value.
* `fb.bool`: creates a field with a boolean as a value.
* `fb.nullValue`: creates a field with a null as a value.
* `fb.array`: creates a field with an array as a value.
* `fb.obj`: creates a field with an object as a value.  

When rendering using a line oriented encoder, `fb.array` and `fb.obj` render in logfmt style `key=value` format, and the other methods use the `value` format.

```scala
import com.tersesystems.echopraxia.plusscala._
import com.tersesystems.echopraxia.plusscala.api._

class Example {
  val logger = LoggerFactory.getLogger

  def doStuff: Unit = {
    logger.info("{} {} {} {}", fb => fb.list {
      import fb._
      obj("person" -> 
        array(
          number("number" -> 1),
          bool("bool" -> true),
          array("ints" -> Seq(1, 2, 3)),
          string("strName" -> "bar")
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
        fb.string("category", "reference"),
        fb.string("author", "Nigel Rees"),
        fb.string("title", "Sayings of the Century"),
        fb.number("price", 8.95)
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

Although using the default field builder is great for one-offs, if you want to log more complex objects it can be tedious to render a large object down to its component parts.  To make it easier, Echopraxia incorporates custom fields builders that can be domain specific.

You can create your own field builder and define type class instances, using `ToValue` and `ToObjectValue`.

```scala
case class Book(category: String, author: String, title: String, price: Double)

trait CustomFieldBuilder extends FieldBuilder {
  implicit val instantToStringValue: ToValue[Instant] = 
    (t: Instant) => ToValue(t.toString)

  def instant(name: String, i: Instant): Field = 
    keyValue(name, ToValue(i))

  def instant(tuple: (String, Instant)): Field =
    keyValue(tuple)

  implicit val bookToObjectValue: ToObjectValue[Book] = { book =>
    // use keyValue when you want to override the field to include the name
    ToObjectValue(
      keyValue("category", book.category),
      keyValue("author", book.author),
      keyValue("title", book.title),
      keyValue("price", book.price)
    )
  }

  def book(name: String, book: Book): Field = 
    keyValue(name, ToValue(book))
}
object CustomFieldBuilder extends CustomFieldBuilder
```

And then you render an instant:

```scala
logger.info("time {}", fb.instant("current", Instant.now))
```

You can also extend the field builder for more general purposes, for example to treat all `Map[String, V]` as objects:

```scala
trait MapFieldBuilder extends FieldBuilder {
  implicit def mapToObjectValue[V: ToValue]: ToObjectValue[Map[String, V]] = 
    m => ToObjectValue(m.map(keyValue))
}
```

or make `Option[V]` return either a value or a null: 

```scala
trait OptionFieldBuilder extends FieldBuilder {
  implicit def optionToValue[V: ToValue]: ToValue[Option[V]] = {
    case Some(v) => ToValue(v)
    case None => Value.nullValue()
  }
}
```

## Conditions

Conditions in the Scala API use Scala idioms and classes.  The `find` methods in the logging context are converted to Scala, so `java.math.BigInteger` is converted to `BigInt`, for example:

```scala
val bigIntCondition = Condition(_.findNumber("$.bigInt").contains(BigInt("52")))
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

Because the JVM is very good at optimizing out no-op methods, using `Condition.never` can enable zero-overhead logging, in the style of [zerolog](https://github.com/obsidiandynamics/zerolog).

## Source Info

Both the logger and the async logger take the source code location, file, and enclosing method as implicits, using [sourcefile](https://github.com/com-lihaoyi/sourcecode).  For example, the `DefaultLoggerMethods.error` method looks like this:

```scala
trait DefaultLoggerMethods[FB] extends LoggerMethods[FB] {
  this: DefaultMethodsSupport[FB] =>

  def error(
      message: String
  )(implicit line: sourcecode.Line, file: sourcecode.File, enc: sourcecode.Enclosing): Unit
  
}
```

Internally, the source code lines are mapped to fields included with the logger, defined with keys from `SourceFieldConstants`:

```scala
trait DefaultLoggerMethods[FB] extends LoggerMethods[FB] {
  // ...
  protected def sourceInfoFields(
      line: Line,
      file: File,
      enc: Enclosing
  ): java.util.function.Function[FB, FieldBuilderResult] = { fb: FB =>
    Field
      .keyValue(
        SourceFieldConstants.sourcecode,
        Value.`object`(
          Field.keyValue(SourceFieldConstants.file, Value.string(file.value)),
          Field.keyValue(SourceFieldConstants.line, Value.number(line.value: java.lang.Integer)),
          Field.keyValue(SourceFieldConstants.enclosing, Value.string(enc.value))
        )
      )
      .asInstanceOf[FieldBuilderResult]
  }.asJava
}
```

You can use the source code fields in conditions transparently -- this can be useful in filters where you want to either show or suppress logging statements coming from a method.  For example:

```scala
import com.tersesystems.echopraxia.api.{CoreLogger, CoreLoggerFilter}
import com.tersesystems.echopraxia.plusscala.api.Condition

class MyLoggerFilter extends CoreLoggerFilter {
  private val sourceCodeCondition = Condition { ctx =>
    ctx.findString("$.sourcecode.enclosing").exists(_.endsWith("thisMethod"))
  }

  override def apply(coreLogger: CoreLogger): CoreLogger = coreLogger.withCondition(sourceCodeCondition.asJava)
}
```

will only log if the method is `thisMethod`:

```scala
def thisMethod(): Unit = {
  logger.info("I log if the method is called thisMethod")
}
```

You can change these constants to have different names by overriding the resource bundle.  You can also override the `sourceInfoFields` using a custom logger to change or suppress source code fields entirely.

## Custom Logger

You can create a custom logger which has your own methods and field builders by extending `AbstractLoggerSupport` with `DefaultLoggerMethods`.

```scala
import com.tersesystems.echopraxia.api.{CoreLogger, Caller, CoreLoggerFactory, FieldBuilderResult, Utilities}
import com.tersesystems.echopraxia.plusscala.DefaultLoggerMethods
import com.tersesystems.echopraxia.plusscala.api._

object CustomLoggerFactory {
  private val FQCN: String = classOf[DefaultLoggerMethods[_]].getName
  private val fieldBuilder: CustomFieldBuilder = CustomFieldBuilder

  def getLogger(name: String): CustomLogger = {
    val core = CoreLoggerFactory.getLogger(FQCN, name)
    new CustomLogger(core, fieldBuilder)
  }

  def getLogger(clazz: Class[_]): CustomLogger = {
    val core = CoreLoggerFactory.getLogger(FQCN, clazz.getName)
    new CustomLogger(core, fieldBuilder)
  }

  def getLogger: CustomLogger = {
    val core = CoreLoggerFactory.getLogger(FQCN, Caller.resolveClassName)
    new CustomLogger(core, fieldBuilder)
  }
}

final class CustomLogger(core: CoreLogger, fieldBuilder: CustomFieldBuilder)
  extends AbstractLoggerSupport(core, fieldBuilder)
    with DefaultLoggerMethods[CustomFieldBuilder] {

  private def newLogger(coreLogger: CoreLogger): CustomLogger = 
    new CustomLogger(coreLogger, fieldBuilder)

  def withCondition(scalaCondition: Condition): CustomLogger =
    newLogger(core.withCondition(scalaCondition.asJava))

  def withFields(f: CustomFieldBuilder => FieldBuilderResult): CustomLogger = {
    import scala.compat.java8.FunctionConverters._
    newLogger(core.withFields(f.asJava, fieldBuilder))
  }

  def withThreadContext: CustomLogger = {
    newLogger(core.withThreadContext(Utilities.threadContext()))
  }
}
```

Creating a custom logger can be a good way to ensure that your field builder is used without any extra configuration, and lets you add your own methods and requirements for your application.

You can also provide your own logger from scratch if you want, by only using the API dependency -- this ensures that users only have one logging option and you can add your own metrics and observability around your logging.

```scala
libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "api" % echopraxiaPlusScalaVersion
```
