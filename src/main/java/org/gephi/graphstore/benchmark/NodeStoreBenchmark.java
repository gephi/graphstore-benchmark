package org.gephi.graphstore.benchmark;

import java.util.List;
import java.util.concurrent.TimeUnit;
import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.Node;
import org.gephi.graph.impl.NodeImpl;
import org.gephi.graph.impl.NodeStore;
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
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Thread)
@Fork(warmups = 0, value = 1)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class NodeStoreBenchmark {

    @Param({"100", "1000", "10000", "100000", "1000000"})
    public int nodes;

    private List<Node> nodeList;

    private NodeStore nodeStore;

    @Setup(Level.Iteration)
    public void setUp() {
        final Configuration config = new Configuration();
        config.setEdgeIdType(Integer.class);
        config.setNodeIdType(Integer.class);
        final RandomGraph graph = new RandomGraph(nodes, 0, config).generate();
        nodeStore = graph.getStore().getNodeStore();
        nodeList = graph.getNodes();
    }

    @Benchmark
    @Measurement(iterations = 10)
    @Warmup(iterations = 1)
    @BenchmarkMode(Mode.SingleShotTime)
    public NodeStore pushAll() {
        nodeStore.clear();
        nodeStore.addAll(nodeList);
        return nodeStore;
    }

    @Benchmark
    @Measurement(iterations = 10)
    @Warmup(iterations = 1)
    @BenchmarkMode(Mode.SingleShotTime)
    public NodeStore iterate(Blackhole blackhole) {
        for (Node node : nodeStore) {
            NodeImpl b = (NodeImpl) node;
            blackhole.consume(b);
        }
        return nodeStore;
    }

    @Benchmark
    @Measurement(iterations = 10)
    @Warmup(iterations = 1)
    @BenchmarkMode(Mode.SingleShotTime)
    public NodeStore reset() {
        for (Node node : nodeList) {
            NodeImpl b = (NodeImpl) node;
            nodeStore.remove(b);
        }
        for (Node n : nodeList) {
            nodeStore.add(n);
        }
        return nodeStore;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(NodeStoreBenchmark.class.getSimpleName())
            .addProfiler(MemoryProfiler.class)
            .build();

        new Runner(opt).run();
    }
}
