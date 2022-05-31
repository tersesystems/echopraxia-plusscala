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

## Trace Logger

You can use a trace logger to debug methods and interactions in your code.  

```scala
libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "trace" % echopraxiaPlusScalaVersion
```

This works very well when you want to add "enter" and "exit" logging statements around your method, by adding a block of `traceLogger.trace`.

```scala
import com.tersesystems.echopraxia.plusscala.trace._
val traceLogger = TraceLoggerFactory.getLogger
def myMethod(arg1: String): Int = traceLogger.trace {
  // ... logic
}
```

Trace logging works particularly well with automatic derivation.

The behavior of the trace logger is determined by `TracingFieldBuilder`.  You can extend `DefaultTracingFieldBuilder` and override various bits of functionality, such as `argumentField`:

```scala
object TraceMain {

  trait TraceFieldBuilder extends DefaultTracingFieldBuilder with AutoDerivation {
    override def argumentField(txt: Text[_]): Field = {
      // you can override how arguments are presented
      value(txt.source, Objects.toString(txt.value))
    }
  }
  object TraceFieldBuilder extends TraceFieldBuilder

  private val traceLogger = TraceLoggerFactory.getLogger.withFieldBuilder(TraceFieldBuilder)

  private def createFoo(barValue: String): Foo = traceLogger.trace {
    Foo(Bar(barValue))
  }

  def main(args: Array[String]): Unit = {
    val foo = createFoo("bar value")
    println(s"foo = $foo")
  }
}
```

produces the following:

```
16:45:24.839 TRACE [main]: {method=com.example.TraceMain.createFoo, tag=entry, arguments=[[{bar value}]]}
16:45:24.885 TRACE [main]: {com.example.TraceMain.createFoo, exit, result={bar=bar value}}
foo = Foo(Bar(bar value))
```

## API

You can convert levels, conditions, and logging contexts to Java using the `.asJava` suffix.

Conversion of Java levels, conditions, and logging contexts are handled through type enrichment adding `.asScala` methods to the classes.

To enable type enrichment, import the `api` package,

```scala
import com.tersesystems.echopraxia.plusscala.api._
```

or 

```
import com.tersesystems.echopraxia.plusscala.api.Implicits._
```

explicitly if you only want the implicits.

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

## Automatic Type Class Derivation

Mapping individual case classes and value objects can get repetitive, so there's a shortcut you can use: [type class derivation](https://blog.kaizen-solutions.io/2020/typeclass-derivation-with-magnolia/). 

You can incorporate automatic type class derivation by adding the `AutoDerivation` or `SemiAutoDerivation` trait.  This trait will set up fields and values in case classes and sealed traits appropriately, using [Magnolia](https://github.com/softwaremill/magnolia/tree/scala2).

Automatic derivation applies to all case classes, while semi-automatic derivation requires the type class instance to be derived explicitly:

```scala
import com.tersesystems.echopraxia.plusscala.api._

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

Because the JVM is very good at optimizing out no-op methods, using `Condition.never` can enable zero-overhead logging, in the style of [zerolog](https://github.com/obsidiandynamics/zerolog).

## Source Info

Both the logger and the async logger take the source code location, file, and enclosing method as implicits, using [sourcefile](https://github.com/com-lihaoyi/sourcecode).  For example, the `DefaultLoggerMethods.error` method looks like this:

```scala
import sourcecode._
trait DefaultLoggerMethods[FB <: SourceCodeFieldBuilder] extends LoggerMethods[FB] {
  this: DefaultMethodsSupport[FB] =>
  
  def error(
      message: String
  )(implicit line: Line, file: File, enc: Enclosing): Unit = {
    // ...
  }
  
}
```

Internally, the source code fields delegate to `fb.sourceCodeFields`, which is defined by `SourceCodeFieldBuilder`:

```scala
trait SourceCodeFieldBuilder {
  def sourceCodeFields(line: Int, file: String, enc: String): FieldBuilderResult
}
```

The default implementation is `DefaultSourceCodeFieldBuilder`, which is included as part of `FieldBuilder`.

```scala
trait FieldBuilder extends TupleFieldBuilder with ArgsFieldBuilder with DefaultSourceCodeFieldBuilder
```

You can override or replace the `sourceCodeFields` method in your own field builder implementation.

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

You can also provide your own logger from scratch if you want, by only using the API dependency -- this ensures that users only have one logging option, and you can add your own metrics and observability around your logging.

```scala
libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "api" % echopraxiaPlusScalaVersion
```
