# Scala API for Echopraxia

The Scala API for Echopraxia is a layer over the Java API that works smoothly with Scala types.

## Quick Start

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
  asyncLogger.debug("expensive but worth it {}", fb => fb.name("result", expensiveResult))
}
```

To account for this, async logging handles conditions and expensive queries by returning a handle that ensures all computation happens in the logging thread.  (This is a little different from the Java API, because IntelliJ's Scala mode will get confused by overloaded methods and parameterized types.)

```scala
asyncLogger.ifDebugEnabled { log => // condition evaluation
  val result = expensiveDebugLoggingQuery() // queries in logging thread
  log("async expensive result {}", fb => fb.number("result" -> result)) // handle does not evaluate
}
```

## Field Builder

A field is defined as a `name` and a `value`, where the value can one of the types defined in `Field.Value`.  Defining a value like `StringValue` or `BooleanValue` can be tedious, and so the Scala field builder has methods that take `ToValue`, `ToObjectValue`, and `ToArrayValue` type classes.

The more field builder methods, `fb.string`, `fb.number`, `fb.bool`, `fb.array`, and `fb.obj` have more specific value requirements.  Only `fb.array` and `fb.obj` use `key=value` format, and the other methods use the `value` format.

```scala
import com.tersesystems.echopraxia.plusscala._

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

Arrays will take a `Seq` of values, including object values.  Object values take a sequence of fields as arguments, and are best defined using the `ToObjectValue(fields)` method. For example, the first element in the [path example from Json-Path](https://github.com/json-path/JsonPath#path-examples) can be represented as:

```scala
import com.tersesystems.echopraxia.plusscala.api._

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

You can create your own field builder and define type class instances, using `ToValue` and `ToObjectValue`.

```scala
import com.tersesystems.echopraxia.api.Field
import com.tersesystems.echopraxia.plusscala.api._

case class Book(category: String, author: String, title: String, price: Double)

trait CustomFieldBuilder extends FieldBuilder {
  implicit val instantToStringValue: ToValue[Instant] = 
    (t: Instant) => ToValue(t.toString)

  def instant(name: String, i: Instant): Field = 
    keyValue(name, ToValue(i))

  implicit val bookToObjectValue: ToObjectValue[Book] = { book =>
    ToObjectValue(
      string("category", book.category),
      string("author", book.author),
      string("title", book.title),
      number("price", book.price)
    )
  }

  def book(name: String, book: Book): Field = 
    keyValue(name, ToValue(book))

  implicit def mapToObjectValue[V: ToValue]: ToObjectValue[Map[String, V]] = 
    m => ToObjectValue(m.map(keyValue))
}
object CustomFieldBuilder extends CustomFieldBuilder
```

And then you render an instant:

```scala
logger.info("time {}", fb.instant("current", Instant.now))
```

Or you can import the field builder implicit:

```scala
logger.info("time {}", fb => {
  import fb._
  keyValue("current" -> Instant.now)
})
```

## Custom Logger

You can create a custom logger which has your own methods and field builders by extending `AbstractLoggerSupport` with `DefaultLoggerMethods`.

```scala
import com.tersesystems.echopraxia.api.CoreLogger
import com.tersesystems.echopraxia.plusscala.DefaultLoggerMethods
import com.tersesystems.echopraxia.plusscala.api._

object CustomLoggerFactory {
  import com.tersesystems.echopraxia.api.{Caller, CoreLoggerFactory}

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
  import com.tersesystems.echopraxia.api.{FieldBuilderResult, Utilities}

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

You can also provide your own logger from scratch if you want, by only using the API dependency.

```scala
libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "api" % echopraxiaPlusScalaVersion
```
