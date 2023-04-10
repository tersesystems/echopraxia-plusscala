## NameOf Logger and Field Builders

The "NameOf" logger and field builder will take the name of the variable passed in as the field name, using a macro, following [dwickern/scala-nameof](https://github.com/dwickern/scala-nameof).  These tools can be very helpful paired with IntelliJ [live templates](https://www.jetbrains.com/help/idea/using-live-templates.html) or [Custom Postfix Templates](https://github.com/xylo/intellij-postfix-templates).

To use the NameOf logger or field builder, add the following dependency:

```scala
libraryDependencies += "com.tersesystems.echopraxia.plusscala" %% "nameof" % echopraxiaPlusScalaVersion
```

### NameOf Logger

The NameOf logger will log a single variable at a time, with no arguments.  It has the same effect as `core.log(level, "{}", _.keyValue(variableName, ToValue(variable)))` -- you must have a `ToValue` type class in scope for the logger to work.

For example:

```scala
val logger = NameOfLoggerFactory.getLogger

val emptySeq = Seq.empty[Int]
logger.debug(emptySeq)
```

outputs `emptySeq=[]`: the `emptySeq` identifier is used as the field name, and an empty array as the value.

### NameOf Field Builder

The NameOf field builder adds `nameOfKeyValue`, `nameOfValue`, and `nameOf`.  You can use this to render multiple variables at once:

```scala
val foo: Foo = ...
val bar: Bar = ...
val quux: Quux = ...
val logger = LoggerFactory.getLogger.withFieldBuilder(NameOfFieldBuilder)
logger.debug("{} {}", fb => fb.list(
  fb.nameOfKeyValue(foo),
  fb.nameOfValue(bar),
  fb.nameOf(quux) -> quux
))
```

Please see the field builder section that explains the details of field builders.
