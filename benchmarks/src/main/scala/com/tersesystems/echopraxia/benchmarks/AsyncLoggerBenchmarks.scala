package com.tersesystems.echopraxia.benchmarks

import com.tersesystems.echopraxia.plusscala.api.Condition
import com.tersesystems.echopraxia.plusscala.async.AsyncLoggerFactory
import org.openjdk.jmh.annotations._

import java.util.concurrent.{Executor, TimeUnit}

@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 20, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
class AsyncLoggerBenchmarks {
  import AsyncLoggerBenchmarks._

  @Benchmark
  def info(): Unit = {
    logger.info("Hello world")
  }

  @Benchmark
  def infoWithStringArg(): Unit = {
    logger.info("Hello {}", _.string("name", "world"))
  }

  @Benchmark
  def infoWithIntegerArg(): Unit = {
    logger.info("1 + 1 = {}", _.number("result", 2))
  }

  @Benchmark
  def infoWithBooleanArg(): Unit = {
    logger.info("return {}", _.bool("returnValue", true))
  }

  @Benchmark
  def neverInfo(): Unit = {
    neverLogger.info("Hello world")
  }

  @Benchmark
  def trace(): Unit = {
    logger.trace("Hello world")
  }

  @Benchmark
  def traceWithStringArg(): Unit = {
    logger.trace("Hello {}", _.string("name", "world"))
  }
}

object AsyncLoggerBenchmarks {
  // logger with no executor, at all.
  private val logger = AsyncLoggerFactory.getLogger.withExecutor(new Executor {
    override def execute(command: Runnable): Unit = {}
  })

  private val neverLogger = AsyncLoggerFactory.getLogger.withCondition(Condition.never)
}
