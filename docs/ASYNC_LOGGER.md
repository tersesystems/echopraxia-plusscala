
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
