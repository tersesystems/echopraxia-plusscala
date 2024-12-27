## Trace Logger

You can use a trace or flow logger to debug methods and interactions in your code.  The API between the trace and flow loggers is the same, but the trace logger is considerably more verbose than flow.

This works very well when you want to add "enter" and "exit" logging statements around your method, by adding a block of `traceLogger.trace`.

```scala
import echopraxia.plusscala.trace._
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
import echopraxia.plusscala.generic._
import echopraxia.plusscala.trace._

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
