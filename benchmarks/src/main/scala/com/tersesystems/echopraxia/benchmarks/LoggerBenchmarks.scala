package com.tersesystems.echopraxia.benchmarks

import com.tersesystems.echopraxia.plusscala.LoggerFactory
import com.tersesystems.echopraxia.plusscala.api.Condition

import java.util.concurrent.TimeUnit
import org.openjdk.jmh.annotations._

@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
class LoggerBenchmarks {
  import LoggerBenchmarks._

  @Benchmark
  def info(): Unit = {
    logger.info("Hello world")
  }

  @Benchmark
  def neverInfo(): Unit = {
    neverLogger.info("Hello world")
  }
}

object LoggerBenchmarks {
  private val logger = LoggerFactory.getLogger

  private val neverLogger = LoggerFactory.getLogger.withCondition(Condition.never)
}