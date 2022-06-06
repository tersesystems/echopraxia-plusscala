# Benchmarks

```
[info] Benchmark                                 Mode  Cnt     Score     Error  Units
[info] AsyncLoggerBenchmarks.info                avgt    5    95.434 ±   3.797  ns/op
[info] AsyncLoggerBenchmarks.infoWithBooleanArg  avgt    5   101.591 ±   0.726  ns/op
[info] AsyncLoggerBenchmarks.infoWithIntegerArg  avgt    5   103.525 ±  14.103  ns/op
[info] AsyncLoggerBenchmarks.infoWithSource      avgt    5   107.901 ±   9.406  ns/op
[info] AsyncLoggerBenchmarks.infoWithStringArg   avgt    5   103.351 ±   1.196  ns/op
[info] AsyncLoggerBenchmarks.neverInfo           avgt    5     1.254 ±   0.008  ns/op
[info] AsyncLoggerBenchmarks.trace               avgt    5   109.861 ±   1.687  ns/op
[info] AsyncLoggerBenchmarks.traceWithStringArg  avgt    5   111.784 ±  21.756  ns/op
[info] FlowLoggerBenchmarks.info                 avgt    5   467.641 ±  66.188  ns/op
[info] FlowLoggerBenchmarks.neverInfo            avgt    5     4.890 ±   0.036  ns/op
[info] FlowLoggerBenchmarks.trace                avgt    5    11.426 ±   0.110  ns/op
[info] LoggerBenchmarks.info                     avgt    5   191.060 ±   1.239  ns/op
[info] LoggerBenchmarks.infoWithBooleanArg       avgt    5   260.669 ±  23.719  ns/op
[info] LoggerBenchmarks.infoWithIntegerArg       avgt    5   263.737 ±  15.033  ns/op
[info] LoggerBenchmarks.infoWithSource           avgt    5   385.463 ±  18.627  ns/op
[info] LoggerBenchmarks.infoWithStringArg        avgt    5   274.432 ±   2.439  ns/op
[info] LoggerBenchmarks.neverInfo                avgt    5     1.345 ±   0.012  ns/op
[info] LoggerBenchmarks.trace                    avgt    5   117.077 ±   0.880  ns/op
[info] LoggerBenchmarks.traceWithStringArg       avgt    5   118.577 ±   1.670  ns/op
[info] TraceLoggerBenchmarks.info                avgt    5  1697.357 ± 184.190  ns/op
[info] TraceLoggerBenchmarks.neverInfo           avgt    5    16.166 ±   0.206  ns/op
[info] TraceLoggerBenchmarks.trace               avgt    5    25.556 ±   8.683  ns/op
```