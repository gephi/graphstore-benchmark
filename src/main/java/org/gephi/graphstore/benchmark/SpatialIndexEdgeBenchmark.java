package org.gephi.graphstore.benchmark;

import java.util.concurrent.TimeUnit;
import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Rect2D;
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
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class SpatialIndexEdgeBenchmark {

    @Param({"1.0", "0.5", "0.25"})
    public float spaceFraction;

    private final static int NODES = 100000;
    private final static int EDGES_PER_NODE = 10;
    private final static Rect2D SPACE = new Rect2D(Generator.MINIMUM_POSITION-10, Generator.MINIMUM_POSITION-10, Generator.MAXIMUM_POSITION+10, Generator.MAXIMUM_POSITION+10);

    private GraphStore store;

    @Setup(Level.Trial)
    public void setUp() {
        Configuration configuration = Configuration.builder().
            nodeIdType(Integer.class).
            edgeIdType(Integer.class).
            enableSpatialIndex(true).
            enableAutoLocking(false).build();
        Generator generator = Generator.generate(NODES, NODES * EDGES_PER_NODE);
        store = generator.withConfiguration(configuration).withPositions().build();
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        store = null;
    }

    @Benchmark
    @Measurement(iterations = 3)
    @Warmup(iterations = 1)
    @BenchmarkMode(Mode.AverageTime)
    public GraphStore edgesInAreaClassicIterator(Blackhole blackhole) {
        final Rect2D rect = new Rect2D(
            SPACE.minX* spaceFraction,
            SPACE.minY* spaceFraction,
            SPACE.maxX* spaceFraction,
            SPACE.maxY* spaceFraction);
        for(Edge edge : store.getSpatialIndex().getEdgesInArea(rect)) {
            blackhole.consume(edge);
        }
        blackhole.consume(rect);
        return store;
    }

    @Benchmark
    @Measurement(iterations = 3)
    @Warmup(iterations = 1)
    @BenchmarkMode(Mode.AverageTime)
    public GraphStore edgesApproximateInAreaClassicIterator(Blackhole blackhole) {
        final Rect2D rect = new Rect2D(
            SPACE.minX* spaceFraction,
            SPACE.minY* spaceFraction,
            SPACE.maxX* spaceFraction,
            SPACE.maxY* spaceFraction);
        for(Edge edge : store.getSpatialIndex().getApproximateEdgesInArea(rect)) {
            blackhole.consume(edge);
        }
        blackhole.consume(rect);
        return store;
    }

    @Benchmark
    @Measurement(iterations = 3)
    @Warmup(iterations = 1)
    @BenchmarkMode(Mode.AverageTime)
    public GraphStore edgesInAreaSpliterator(Blackhole blackhole) {
        final Rect2D rect = new Rect2D(
            SPACE.minX* spaceFraction,
            SPACE.minY* spaceFraction,
            SPACE.maxX* spaceFraction,
            SPACE.maxY* spaceFraction);
        store.getSpatialIndex().getEdgesInArea(rect).stream().forEach(blackhole::consume);
        blackhole.consume(rect);
        return store;
    }

    @Benchmark
    @Measurement(iterations = 3)
    @Warmup(iterations = 1)
    @BenchmarkMode(Mode.AverageTime)
    public GraphStore edgesInAreaSpliteratorParallel(Blackhole blackhole) {
        final Rect2D rect = new Rect2D(
            SPACE.minX* spaceFraction,
            SPACE.minY* spaceFraction,
            SPACE.maxX* spaceFraction,
            SPACE.maxY* spaceFraction);
        store.getSpatialIndex().getEdgesInArea(rect).parallelStream().forEach(blackhole::consume);
        blackhole.consume(rect);
        return store;
    }

    @Benchmark
    @Measurement(iterations = 3)
    @Warmup(iterations = 1)
    @BenchmarkMode(Mode.AverageTime)
    public GraphStore edgesApproximateInAreaSpliteratorParallel(Blackhole blackhole) {
        final Rect2D rect = new Rect2D(
            SPACE.minX* spaceFraction,
            SPACE.minY* spaceFraction,
            SPACE.maxX* spaceFraction,
            SPACE.maxY* spaceFraction);
        store.getSpatialIndex().getApproximateEdgesInArea(rect).parallelStream().forEach(blackhole::consume);
        blackhole.consume(rect);
        return store;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(SpatialIndexEdgeBenchmark.class.getSimpleName())
            .build();

        new Runner(opt).run();
    }
}
