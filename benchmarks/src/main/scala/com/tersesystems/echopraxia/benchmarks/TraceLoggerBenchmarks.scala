package com.tersesystems.echopraxia.benchmarks

import com.tersesystems.echopraxia.plusscala.api.Condition
import com.tersesystems.echopraxia.plusscala.trace.TraceLoggerFactory
import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole

import java.util.concurrent.TimeUnit

@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
class TraceLoggerBenchmarks {
  import TraceLoggerBenchmarks._

  @Benchmark
  def info(blackhole: Blackhole): Unit = {
    val result = logger.info {
      "some string"
    }
    blackhole.consume(result)
  }

  @Benchmark
  def infoWithCondition(blackhole: Blackhole): Unit = {
    // should fail condition without ever having to resolve fields,
    // so much faster (although still slower than a level check or "Condition.never")
    val result = logger.info(condition) {
      "some string"
    }
    blackhole.consume(result)
  }

  @Benchmark
  def infoWithFieldsCondition(blackhole: Blackhole): Unit = {
    // this should about the same speed as info because fields
    // are memoized
    val result = logger.info(fieldsCondition) {
      "some string"
    }
    blackhole.consume(result)
  }

  @Benchmark
  def traceWithCondition(blackhole: Blackhole): Unit = {
    val result = logger.trace(condition) {
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

object TraceLoggerBenchmarks {
  private val logger = TraceLoggerFactory.getLogger

  private val condition: Condition = (_, ctx) => false

  private val fieldsCondition: Condition = (_, ctx) => ctx.findString("$.sourcecode.file").exists(s => s.endsWith("TraceLoggerBenchmarks.scala"))

  private val neverLogger = logger.withCondition(Condition.never)
}
