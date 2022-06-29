package com.tersesystems.echopraxia.benchmarks

import com.tersesystems.echopraxia.plusscala.LoggerFactory
import com.tersesystems.echopraxia.plusscala.api.Condition
import org.openjdk.jmh.annotations._

import java.util.concurrent.TimeUnit

@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 20, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
class LoggerBenchmarks {
  import LoggerBenchmarks._

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

object LoggerBenchmarks {
  private val logger = LoggerFactory.getLogger

  private val neverLogger = LoggerFactory.getLogger.withCondition(Condition.never)
}
