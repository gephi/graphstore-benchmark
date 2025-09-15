package org.gephi.graphstore.benchmark;

import java.util.concurrent.TimeUnit;
import org.gephi.graph.api.Node;
import org.gephi.graph.impl.GraphStore;
import org.gephi.graph.impl.NodeImpl;
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
public class NodeStoreBenchmark {

    @Param({"100", "1000", "10000", "100000", "1000000"})
    public int nodes;

    private GraphStore store;

    @Setup(Level.Trial)
    public void setUp() {
        Generator generator = Generator.generate(nodes, 0);
        store = generator.withOnlyNodes().build();
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        store = null;
    }


    @Benchmark
    @Measurement(iterations = 8)
    @Warmup(iterations = 1)
    @BenchmarkMode(Mode.AverageTime)
    public GraphStore iterate(Blackhole blackhole) {
        for (Node node : store.getNodes()) {
            NodeImpl b = (NodeImpl) node;
            blackhole.consume(b);
        }
        return store;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(NodeStoreBenchmark.class.getSimpleName())
            .build();

        new Runner(opt).run();
    }
}
