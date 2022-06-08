# Benchmarks

```
[info] Benchmark                                 Mode  Cnt     Score    Error  Units
[info] AsyncLoggerBenchmarks.info                avgt   20    89.934 ±  1.344  ns/op
[info] AsyncLoggerBenchmarks.infoWithBooleanArg  avgt   20   104.725 ±  2.377  ns/op
[info] AsyncLoggerBenchmarks.infoWithIntegerArg  avgt   20   103.899 ±  3.197  ns/op
[info] AsyncLoggerBenchmarks.infoWithSource      avgt   20   103.367 ±  2.734  ns/op
[info] AsyncLoggerBenchmarks.infoWithStringArg   avgt   20   109.117 ±  3.096  ns/op
[info] AsyncLoggerBenchmarks.neverInfo           avgt   20     1.255 ±  0.002  ns/op
[info] AsyncLoggerBenchmarks.trace               avgt   20     7.177 ±  0.036  ns/op
[info] AsyncLoggerBenchmarks.traceWithStringArg  avgt   20     7.613 ±  0.043  ns/op
[info] FlowLoggerBenchmarks.info                 avgt   20   427.362 ±  3.829  ns/op
[info] FlowLoggerBenchmarks.neverInfo            avgt   20     4.861 ±  0.157  ns/op
[info] FlowLoggerBenchmarks.trace                avgt   20    10.365 ±  0.116  ns/op
[info] LoggerBenchmarks.info                     avgt   20   193.176 ±  0.919  ns/op
[info] LoggerBenchmarks.infoWithBooleanArg       avgt   20   340.025 ±  5.697  ns/op
[info] LoggerBenchmarks.infoWithIntegerArg       avgt   20   336.608 ±  0.956  ns/op
[info] LoggerBenchmarks.infoWithSource           avgt   20   441.175 ± 13.766  ns/op
[info] LoggerBenchmarks.infoWithStringArg        avgt   20   294.614 ±  7.904  ns/op
[info] LoggerBenchmarks.neverInfo                avgt   20     1.234 ±  0.007  ns/op
[info] LoggerBenchmarks.trace                    avgt   20     8.005 ±  0.035  ns/op
[info] LoggerBenchmarks.traceWithStringArg       avgt   20     7.757 ±  0.273  ns/op
[info] TraceLoggerBenchmarks.info                avgt   20  1601.620 ±  7.476  ns/op
[info] TraceLoggerBenchmarks.neverInfo           avgt   20    16.946 ±  0.377  ns/op
[info] TraceLoggerBenchmarks.trace               avgt   20    23.205 ±  0.083  ns/op
```

