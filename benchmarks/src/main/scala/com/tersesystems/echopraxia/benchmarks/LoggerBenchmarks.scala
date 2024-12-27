package com.tersesystems.echopraxia.benchmarks

import echopraxia.plusscala.api.LoggingBase
import echopraxia.plusscala.logger.LoggerFactory
import echopraxia.plusscala.logging.api.Condition
import org.openjdk.jmh.annotations._

import java.util.concurrent.TimeUnit

@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 20, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
class LoggerBenchmarks extends LoggingBase {
  import LoggerBenchmarks._

  @Benchmark
  def info(): Unit = {
    logger.info("Hello world")
  }

  @Benchmark
  def infoWithStringArg(): Unit = {
    logger.info("Hello {}", ("name" -> "world"))
  }

  @Benchmark
  def infoWithIntegerArg(): Unit = {
    logger.info("1 + 1 = {}", "result" -> 2)
  }

  @Benchmark
  def infoWithBooleanArg(): Unit = {
    logger.info("return {}", "returnValue" -> true)
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
    logger.trace("Hello {}", "name" -> "world")
  }

}

object LoggerBenchmarks {
  private val logger = LoggerFactory.getLogger

  private val neverLogger = LoggerFactory.getLogger.withCondition(Condition.never)
}
