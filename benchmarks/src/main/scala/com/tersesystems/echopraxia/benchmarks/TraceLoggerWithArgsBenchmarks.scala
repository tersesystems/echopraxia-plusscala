package com.tersesystems.echopraxia.benchmarks

import com.tersesystems.echopraxia.plusscala.api.Condition
import com.tersesystems.echopraxia.plusscala.trace.TraceLoggerWithArgsFactory
import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole

import java.util.concurrent.TimeUnit

@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
class TraceLoggerWithArgsBenchmarks {
  import TraceLoggerWithArgsBenchmarks._

  @Benchmark
  def info(blackhole: Blackhole): Unit = {
    val result = logger.info {
      "some string"
    }
    blackhole.consume(result)
  }

  @Benchmark
  def neverInfo(blackhole: Blackhole): Unit = {
    val result = neverLogger.info {
      "some string"
    }
    blackhole.consume(result)
  }

  @Benchmark
  def trace(blackhole: Blackhole): Unit = {
    val result = logger.trace {
      "some string"
    }
    blackhole.consume(result)
  }

}


object TraceLoggerWithArgsBenchmarks {
  private val logger = TraceLoggerWithArgsFactory.getLogger

  private val neverLogger = logger.withCondition(Condition.never)
}