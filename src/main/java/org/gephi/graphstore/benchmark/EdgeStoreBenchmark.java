package org.gephi.graphstore.benchmark;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.graph.impl.EdgeStore;
import org.gephi.graphstore.benchmark.util.MemoryProfiler;
import org.gephi.graphstore.benchmark.util.RandomGraph;
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
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class EdgeStoreBenchmark {

    @Param({"100", "1000", "10000", "100000"})
    public int nodes;

    private List<Node> nodeList;

    private List<Edge> edgeList;

    private EdgeStore edgeStore;

    @Setup(Level.Iteration)
    public void setUp() {
        final Configuration config = new Configuration();
        config.setEdgeIdType(Integer.class);
        config.setNodeIdType(Integer.class);
        final RandomGraph graph = new RandomGraph(nodes, 0, config).generate();
        edgeStore = graph.getStore().getEdgeStore();
        nodeList = graph.getNodes();
        edgeList = graph.getEdges();
        graph.getStore().addAllNodes(nodeList);
    }

    @TearDown(Level.Iteration)
    public void tearDown() {
        edgeStore.clear();
        nodeList = null;
        edgeStore = null;
    }

    @Benchmark
    @Measurement(iterations = 10)
    @Warmup(iterations = 1)
    @BenchmarkMode(Mode.SingleShotTime)
    public EdgeStore pushAll() {
        edgeStore.clear();
        edgeStore.addAll(edgeList);
        return edgeStore;
    }

    @Benchmark
    @Measurement(iterations = 10)
    @Warmup(iterations = 1)
    @BenchmarkMode(Mode.SingleShotTime)
    public EdgeStore iterate(Blackhole blackhole) {
        for (Edge edge : edgeStore) {
            blackhole.consume(edge);
        }
        return edgeStore;
    }

    @Benchmark
    @Measurement(iterations = 10)
    @Warmup(iterations = 1)
    @BenchmarkMode(Mode.SingleShotTime)
    public EdgeStore iterateNeighborsOut(Blackhole blackhole) {
        for (Node node : nodeList) {
            Iterator<Edge> itr = edgeStore.edgeOutIterator(node);
            while (itr.hasNext()) {
                blackhole.consume(itr.next());
            }
        }
        return edgeStore;
    }

    @Benchmark
    @Measurement(iterations = 10)
    @Warmup(iterations = 1)
    @BenchmarkMode(Mode.SingleShotTime)
    public EdgeStore iterateNeighborsInOut(Blackhole blackhole) {
        for (Node node : nodeList) {
            Iterator<Edge> itr = edgeStore.edgeIterator(node);
            while (itr.hasNext()) {
                blackhole.consume(itr.next());
            }
        }
        return edgeStore;
    }

    @Benchmark
    @Measurement(iterations = 10)
    @Warmup(iterations = 1)
    @BenchmarkMode(Mode.SingleShotTime)
    public EdgeStore reset() {
        for (Edge e : edgeList) {
            edgeStore.remove(e);
        }
        for (Edge e : edgeList) {
            edgeStore.add(e);
        }
        return edgeStore;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(NodeStoreBenchmark.class.getSimpleName())
            .addProfiler(MemoryProfiler.class)
            .build();

        new Runner(opt).run();
    }
}
