package org.gephi.graphstore.benchmark;

import java.util.BitSet;
import java.util.concurrent.TimeUnit;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Subgraph;
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
public class NodeViewStreamBenchmark {

    @Param({ "100000", "1000000"})
    public int nodes;

    @Param({ "0.5" })
    public float fillRate;

    private GraphStore store;
    private Subgraph subgraph;

    @Setup(Level.Trial)
    public void setUp() {
        Generator generator = Generator.generate(nodes, 0);
        store = generator.withOnlyNodes().build();
        GraphView view = store.getModel().createView(true, false);
        subgraph = store.getModel().getGraph(view);
        assert subgraph.getNodeCount() == 0;
        for (Node n : store.getNodes()) {
            if (Math.random() > fillRate) {
                subgraph.addNode(n);
            }
        }
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        store = null;
        subgraph = null;
    }

    @Benchmark
    @Measurement(iterations = 5)
    @Warmup(iterations = 1)
    @BenchmarkMode(Mode.AverageTime)
    public GraphStore iterateClassic(Blackhole blackhole) {
        for (Node node : subgraph.getNodes()) {
            blackhole.consume(node);
        }
        return store;
    }

    @Benchmark
    @Measurement(iterations = 5)
    @Warmup(iterations = 1)
    @BenchmarkMode(Mode.AverageTime)
    public GraphStore iterateParallelForEach(Blackhole blackhole) {
        subgraph.getNodes().parallelStream().forEach(blackhole::consume);
        return store;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(NodeViewStreamBenchmark.class.getSimpleName())
            .build();

        new Runner(opt).run();
    }
}
