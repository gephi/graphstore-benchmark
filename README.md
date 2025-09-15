# GraphStore Benchmark

[JMH](https://github.com/openjdk/jmh) based micro-benchmark for the [GraphStore](https://github.com/gephi/graphstore)
library.

## Build

    > mvn clean install

## Run

    > java -jar target/benchmarks.jar -prof org.gephi.graphstore.benchmark.util.MemoryProfiler

The extra memory profiler is optional and will print the memory usage of the benchmarked methods.

## Changelog

* 0.1.0
    * Initial release, simply porting what we used to have in the GraphStore repository.

## Contribute

The source code is available under the Apache 2.0 license. Contributions are welcome.
