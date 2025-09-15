package org.gephi.graphstore.benchmark;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.TimeUnit;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.graph.impl.GraphStore;
import org.gephi.graphstore.benchmark.util.Generator;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Thread)
@Fork(warmups = 0, value = 1)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class EdgeStoreBenchmark {

    @Param({"1000", "10000", "100000"})
    public int nodes;

    private final static int EDGES_PER_NODE = 10;
    private final static int TYPE_COUNT = 2;

    private GraphStore store;

    @Setup(Level.Trial)
    public void setUp() {
        Generator generator = Generator.generate(nodes, nodes * EDGES_PER_NODE, TYPE_COUNT);
        store = generator.build();
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        store = null;
    }

    @Benchmark
    @Measurement(iterations = 5)
    @Warmup(iterations = 1)
    @BenchmarkMode(Mode.AverageTime)
    public GraphStore iterate(Blackhole blackhole) {
        for (Edge edge : store.getEdges()) {
            blackhole.consume(edge);
        }
        return store;
    }

    @Benchmark
    @Measurement(iterations = 5)
    @Warmup(iterations = 1)
    @BenchmarkMode(Mode.AverageTime)
    public GraphStore iterateViaStream(Blackhole blackhole) {
        Spliterators.spliteratorUnknownSize(store.getEdges().iterator(), Spliterator.NONNULL | Spliterator.ORDERED)
            .forEachRemaining(
                blackhole::consume);
        return store;
    }

    @Benchmark
    @Measurement(iterations = 5)
    @Warmup(iterations = 1)
    @BenchmarkMode(Mode.AverageTime)
    public GraphStore iterateNeighborsOut(Blackhole blackhole) {
        for (Node node : store.getNodes()) {
            for (Edge edge : store.getOutEdges(node)) {
                blackhole.consume(edge);
            }
            blackhole.consume(node);
        }
        return store;
    }

    @Benchmark
    @Measurement(iterations = 5)
    @Warmup(iterations = 1)
    @BenchmarkMode(Mode.AverageTime)
    public GraphStore iterateNeighborsInOut(Blackhole blackhole) {
        for (Node node : store.getNodes()) {
            for (Edge edge : store.getEdges(node)) {
                blackhole.consume(edge);
            }
            blackhole.consume(node);
        }
        return store;
    }

    @Benchmark
    @Measurement(iterations = 5)
    @Warmup(iterations = 1)
    @BenchmarkMode(Mode.AverageTime)
    public GraphStore iterateType(Blackhole blackhole) {
        for (int type = 0; type < TYPE_COUNT; type++) {
            for (Edge edge : store.getEdges(type)) {
                blackhole.consume(edge);
            }
        }
        return store;
    }


    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(EdgeStoreBenchmark.class.getSimpleName())
            .build();

        new Runner(opt).run();
    }
}
