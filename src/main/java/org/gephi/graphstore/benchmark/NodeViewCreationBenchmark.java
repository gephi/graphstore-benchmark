package org.gephi.graphstore.benchmark;

import java.util.concurrent.TimeUnit;
import org.gephi.graph.api.DirectedSubgraph;
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
public class NodeViewCreationBenchmark {

    @Param({ "100000", "1000000"})
    public int nodes;

    @Param({ "0.5" })
    public float fillRate;

    private GraphStore store;
    private GraphView view1;
    private GraphView view2;

    @Setup(Level.Trial)
    public void setUp() {
        Generator generator = Generator.generate(nodes, 0);
        store = generator.withOnlyNodes().build();
        view1 = createView(0.75f);
        view2 = createView(0.70f);
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        store = null;
    }

    private GraphView createView(float localFillRate) {
        GraphView view = store.getModel().createView(true, false);
        Subgraph subgraph = store.getModel().getGraph(view);
        for (Node n : store.getNodes()) {
            if (Math.random() > localFillRate) {
                subgraph.addNode(n);
            }
        }
        return view;
    }

    @Benchmark
    @Measurement(iterations = 5)
    @Warmup(iterations = 1)
    @BenchmarkMode(Mode.AverageTime)
    public GraphStore createLegacy(Blackhole blackhole) {
        GraphView view = createView(fillRate);
        blackhole.consume(view);
        return store;
    }

    @Benchmark
    @Measurement(iterations = 5)
    @Warmup(iterations = 1)
    @BenchmarkMode(Mode.AverageTime)
    public GraphStore createNew(Blackhole blackhole) {
        GraphView view = store.getModel().createView((node) -> Math.random() > fillRate, (edge) -> Math.random() > fillRate);
        blackhole.consume(view);
        return store;
    }

    @Benchmark
    @Measurement(iterations = 5)
    @Warmup(iterations = 1)
    @BenchmarkMode(Mode.AverageTime)
    public GraphStore and(Blackhole blackhole) {
        DirectedSubgraph sub1 = store.getModel().getDirectedGraph(view1);
        DirectedSubgraph sub2 = store.getModel().getDirectedGraph(view2);
        sub1.intersection(sub2);
        blackhole.consume(sub1);
        blackhole.consume(sub2);
        return store;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(NodeViewCreationBenchmark.class.getSimpleName())
            .build();

        new Runner(opt).run();
    }
}
