# Scala API for Echopraxia

[Echopraxia](https://github.com/tersesystems/echopraxia) is a structured logging framework with implementations for Logback and Log4J.  The Scala API for [Echopraxia](https://github.com/tersesystems/echopraxia) is a layer over the Java API that works smoothly with Scala types and has a number of features to make debugging even smoother, including a "trace" logger and automatic type class derivation.  

Echopraxia is compiled for Scala 2.12 and Scala 2.13.  

## Examples

Examples are available at [tersesystems/echopraxia-scala-example](https://github.com/tersesystems/echopraxia-scala-example) and [tersesystems/echopraxia-examples](https://github.com/tersesystems/echopraxia-examples).

## What Does It Mean?

Q: What does this mean for developers?

A: You can write code faster and with fewer bugs by using Echopraxia for debugging.  Echopraxia is oriented for "printf debugging", so all the `println` and `toString` methods that go into your code at development can be entered as logging statements.  Because logging statements are functions, they're only executed when they meet logging conditions, so the cost is low.   And because you can define fields using custom field builders, you can dump your internal state easily.  You can also easily disable a logger completely by adding `Condition.never`, which will switch it to a `no-op` statement.

Q: What does this mean for operations?

A: Echopraxia makes your application more observable through structured logging and automatic "call-by-name" methods that can capture request and trace contexts.  Echopraxia also targets managing logging on a budget -- determining "when to log" on an already deployed application.  All logging statements in Echopraxia are based around fields and values, and can incorporate complex conditional logic that can alter [logging at runtime](https://github.com/tersesystems/dynamic-debug-logging), down to individual statements.

Q: Is this a replacement for SLF4J / Log4J / Logback?

A: You can use Echopraxia and your usual logging framework side by side in the same application.  Echopraxia sends correctly formatted structured logging messages to the underlying framework and is well behaved, tracking the implementations enabled logging levels and filters.

## Logger

Add the following to your `build.sbt` file:

```scala
libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "logger" % echopraxiaPlusScalaVersion
```

and one of the underlying core logger providers, i.e.

```scala
// uncomment only one of these
// libraryDependencies += "com.tersesystems.echopraxia" % "logstash" % "2.1.0"
// OR
// libraryDependencies += "com.tersesystems.echopraxia" % "log4j" % "2.1.0"
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

The Scala logger (technically, the default field builder) has some enhancements over the Java logger, notably the ability to add tuples.  

```scala
logger.info("hi {}", _.string("name", "will")) // two args
logger.info("hi {}", _.string("name" -> "will")) // tuple
```

This can come in very handy when using collections of tuples, because you can map them directly to fields:

```scala
val seq = Seq("first" -> 1, "second" -> 2)
logger.info("seq = {}", fb => fb.list(seq.map(fb.number)))
```

Please see the custom field builder section for more details on building fields.

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

## Trace and Flow Loggers

You can use a trace or flow logger to debug methods and interactions in your code.  The API between the trace and flow loggers is the same, but the trace logger is considerably more verbose than flow.

This works very well when you want to add "enter" and "exit" logging statements around your method, by adding a block of `traceLogger.trace`.

```scala
import com.tersesystems.echopraxia.plusscala.trace._
val traceLogger = TraceLoggerFactory.getLogger
def myMethod(arg1: String): Int = traceLogger.trace {
  // ... logic
}
```

The trace logger includes source information that has a small (~28 nanosecond) runtime impact even when disabled with `Condition.never`.  The flow logger will pass through and has virtually no impact when disabled, either by using `Condition.never` or by logging below threshold.

### Trace Logger

Trace logging usually involves a custom field builder that has additional type classes to handle the return type -- this works particularly well with automatic derivation.  Trace logging includes source code information, **including arguments**, so it is only for use in a debugging situation.

```scala
libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "trace-logger" % echopraxiaPlusScalaVersion
```

The following program extends the DefaultTraceFieldBuilder to use automatic derivation, useful for mapping return values: 

```scala
import com.tersesystems.echopraxia.plusscala.generic._
import com.tersesystems.echopraxia.plusscala.trace._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

object TraceMain {
  import ExecutionContext.Implicits._

  trait AutoTraceFieldBuilder extends DefaultTraceFieldBuilder with AutoDerivation
  object AutoTraceFieldBuilder extends AutoTraceFieldBuilder

  private val logger = TraceLoggerFactory.getLogger.withFieldBuilder(AutoTraceFieldBuilder)

  private def createFoo(barValue: String): Foo = logger.trace {
    Foo(Bar(barValue))
  }

  private def getBar(foo: Foo): Bar = logger.trace {
    foo.bar
  }

  private def noArgsBar: Bar = logger.trace {
    Bar("noArgsBar")
  }

  private def someFuture: Future[Bar] = Future {
    logger.trace {
      Bar("futureBar")
    }
  }

  def main(args: Array[String]): Unit = {
    val foo = createFoo("bar")
    getBar(foo)
    noArgsBar

    Await.result(someFuture, Duration.Inf)
  }
}
```

This renders input and output automatically with arguments included:

```
13:41:35.430 com.example.TraceMain$ TRACE [main]: entry: com.example.TraceMain.createFoo(barValue: String) - (bar)
13:41:35.443 com.example.TraceMain$ TRACE [main]: exit: com.example.TraceMain.createFoo(barValue: String) - (bar) => {@type=com.example.Foo, bar=bar}
13:41:35.446 com.example.TraceMain$ TRACE [main]: entry: com.example.TraceMain.getBar(foo: Foo) - (Foo(Bar(bar)))
13:41:35.447 com.example.TraceMain$ TRACE [main]: exit: com.example.TraceMain.getBar(foo: Foo) - (Foo(Bar(bar))) => bar
13:41:35.449 com.example.TraceMain$ TRACE [main]: entry: com.example.TraceMain.noArgsBar() - ()
13:41:35.449 com.example.TraceMain$ TRACE [main]: exit: com.example.TraceMain.noArgsBar() - () => noArgsBar
13:41:35.489 com.example.TraceMain$ TRACE [scala-execution-context-global-15]: entry: com.example.TraceMain.someFuture() - ()
13:41:35.490 com.example.TraceMain$ TRACE [scala-execution-context-global-15]: exit: com.example.TraceMain.someFuture() - () => futureBar
```

You can override the default behavior by implementing `TraceFieldBuilder`, which takes implicit source code arguments -- this is the primary difference between the trace logger and the flow logger.

### Flow Logger

The flow logger does not contain source code information, but simply renders enter and exit information.  

To add the flow logger to your project, add the following dependency:

```scala
libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "flow-logger" % echopraxiaPlusScalaVersion
```

This is usually more useful in flow situations like a `Future`, where the enclosing method name and arguments are not as useful:

```scala
object FlowMain {
  import ExecutionContext.Implicits._

  trait AutoFlowFieldBuilder extends DefaultFlowFieldBuilder with AutoDerivation
  object AutoFlowFieldBuilder extends AutoFlowFieldBuilder

  private val logger = FlowLoggerFactory.getLogger.withFieldBuilder(AutoFlowFieldBuilder)

  private def createFoo(barValue: String): Foo = logger.trace {
    Foo(Bar(barValue))
  }

  private def getBar(foo: Foo): Bar = logger.trace {
    foo.bar
  }

  private def noArgsBar: Bar = logger.trace {
    Bar("noArgsBar")
  }

  private def someFuture: Future[Bar] = Future {
    logger.trace {
      Bar("futureBar")
    }
  }

  def main(args: Array[String]): Unit = {
    val foo = createFoo("bar")
    getBar(foo)
    noArgsBar

    Await.result(someFuture, Duration.Inf)
  }
}
```

To use the flow logger, add the following dependency:

```scala
libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "flow-logger" % echopraxiaPlusScalaVersion
```

The above program produces the following output:

```
13:39:55.665 com.example.FlowMain$ TRACE [main]: entry
13:39:55.691 com.example.FlowMain$ TRACE [main]: exit => {@type=com.example.Foo, bar=bar}
13:39:55.694 com.example.FlowMain$ TRACE [main]: entry
13:39:55.694 com.example.FlowMain$ TRACE [main]: exit => bar
13:39:55.696 com.example.FlowMain$ TRACE [main]: entry
13:39:55.696 com.example.FlowMain$ TRACE [main]: exit => noArgsBar
13:39:55.735 com.example.FlowMain$ TRACE [scala-execution-context-global-15]: entry
13:39:55.735 com.example.FlowMain$ TRACE [scala-execution-context-global-15]: exit => futureBar
```

The flow logger is not as detailed, but works well in FP situations, where the logger name is unique and there is only one method to call.

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

## Field Builder

A `Field` is defined as a `name: String` and a `value: com.tersesystems.echopraxia.api.Value`.  The field builder has methods to create fields. 

* `fb.string`: creates a field with a string as a value, same as `fb.value(name, Value.string(str))`.
* `fb.number`: creates a field with a number as a value, same as `fb.value(name, Value.number(num))`.
* `fb.bool`: creates a field with a boolean as a value, same as `fb.value(name, Value.bool(b))`.
* `fb.nullValue`: creates a field with a null as a value, same as `fb.value(name, Value.nullValue())`
* `fb.array`: creates a field with an array as a value, same as `fb.keyValue(name, Value.array(arr))`
* `fb.obj`: creates a field with an object as a value, same as `fb.keyValue(name, Value.``object``(o))`

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

Although using the default field builder is great for one-offs, if you want to log more complex objects it can be tedious to render a large object down to its component parts.  To make it easier, Echopraxia incorporates custom fields builders that can be domain specific, and allows you to stack them together.

You can create your own field builder and define type class instances, using `ToValue` and `ToObjectValue`.  Using Scala 2.13, you can log arbitrary classes `Foo` and `Bar`:

```scala
import com.tersesystems.echopraxia.plusscala.LoggerFactory
import com.tersesystems.echopraxia.plusscala.api.FieldBuilder

object Main {
  private val logger = LoggerFactory.getLogger.withFieldBuilder(MyFieldBuilder)

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

  private val logger = LoggerFactory.getLogger.withFieldBuilder(SomeObjectBuilder)

  def main(args: Array[String]): Unit = {
    logger.debug("Hello world!")

    val foo = Foo("name", 1)
    val bar = Bar(true, 0x1)
    
    logger.debug("using method {} {}", _.method(foo, bar))
    logger.debug("using function {} {}", _.function(foo, bar))
  }
}
```

You can also extend the field builder for more general purposes, for example to treat all `Map[String, V]` as objects:

```scala
trait MapFieldBuilder extends FieldBuilder {
  implicit def mapToObjectValue[V: ToValue]: ToObjectValue[Map[String, V]] = 
    m => ToObjectValue(m.map(keyValue))
}
```

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

You can also provide your own logger from scratch if you want, by only using the API dependency -- this ensures that users only have one logging option, and you can add your own metrics and observability around your logging.

```scala
libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "api" % echopraxiaPlusScalaVersion
```
