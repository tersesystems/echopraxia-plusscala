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

