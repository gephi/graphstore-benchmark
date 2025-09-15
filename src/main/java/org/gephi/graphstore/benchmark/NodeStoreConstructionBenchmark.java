package org.gephi.graphstore.benchmark;

import java.util.concurrent.TimeUnit;
import org.gephi.graph.api.GraphFactory;
import org.gephi.graph.impl.GraphStore;
import org.gephi.graphstore.benchmark.draft.DraftNode;
import org.gephi.graphstore.benchmark.util.Generator;
import org.gephi.graphstore.benchmark.util.MemoryProfiler;
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
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Thread)
@Fork(warmups = 0, value = 1)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class NodeStoreConstructionBenchmark {

    @Param({"100", "1000", "10000", "100000", "1000000"})
    public int nodes;

    private Generator generator;

    @Setup(Level.Trial)
    public void setUpTrial() {
        generator = Generator.generate(nodes, 0).withNothing();
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        generator = null;
    }

    @Benchmark
    @Measurement(iterations = 10)
    @Warmup(iterations = 1)
    @BenchmarkMode(Mode.SingleShotTime)
    public GraphStore pushAll() {
        final GraphStore store = generator.build();
        final GraphFactory factory = store.getModel().factory();

        for (DraftNode dn : generator.getNodes()) {
            store.addNode(factory.newNode(dn.getId()));
        }

        return store;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(NodeStoreConstructionBenchmark.class.getSimpleName())
            .addProfiler(MemoryProfiler.class)
            .build();

        new Runner(opt).run();
    }
}
