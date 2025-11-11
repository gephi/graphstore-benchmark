package org.gephi.graphstore.benchmark;

import java.util.concurrent.TimeUnit;
import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Rect2D;
import org.gephi.graph.impl.GraphStore;
import org.gephi.graph.impl.SpatialIndexImpl;
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
public class SpatialIndexNodeViewBenchmark {

    @Param({"1.0"})
    public float spaceFraction;

    private final static int NODES = 1000000;
    private final static Rect2D SPACE = new Rect2D(Generator.MINIMUM_POSITION-10, Generator.MINIMUM_POSITION-10, Generator.MAXIMUM_POSITION+10, Generator.MAXIMUM_POSITION+10);

    private GraphStore store;

    @Setup(Level.Trial)
    public void setUp() {
        Configuration configuration = Configuration.builder().
            nodeIdType(Integer.class).
            edgeIdType(Integer.class).
            enableSpatialIndex(true).
            enableAutoLocking(false).build();
        Generator generator = Generator.generate(NODES, 0);
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
    public GraphStore nodesInAreaWithoutPredicateClassicIterator(Blackhole blackhole) {
        final Rect2D rect = new Rect2D(
            SPACE.minX* spaceFraction,
            SPACE.minY* spaceFraction,
            SPACE.maxX* spaceFraction,
            SPACE.maxY* spaceFraction);

        for(Node node : store.getSpatialIndex().getNodesInArea(rect)) {
            blackhole.consume(node);
        }
        blackhole.consume(rect);
        return store;
    }

    @Benchmark
    @Measurement(iterations = 3)
    @Warmup(iterations = 1)
    @BenchmarkMode(Mode.AverageTime)
    public GraphStore nodesInAreaWithPredicateClassicIterator(Blackhole blackhole) {
        final Rect2D rect = new Rect2D(
            SPACE.minX* spaceFraction,
            SPACE.minY* spaceFraction,
            SPACE.maxX* spaceFraction,
            SPACE.maxY* spaceFraction);
        SpatialIndexImpl spatialIndex = (SpatialIndexImpl) store.getSpatialIndex();

        for(Node node : spatialIndex.getNodesInArea(rect, (node) -> node.getStoreId() % 2 == 0)) {
            blackhole.consume(node);
        }
        blackhole.consume(rect);
        blackhole.consume(spatialIndex);
        return store;
    }

    @Benchmark
    @Measurement(iterations = 3)
    @Warmup(iterations = 1)
    @BenchmarkMode(Mode.AverageTime)
    public GraphStore nodesApproximateInAreaWithoutPredicateClassicIterator(Blackhole blackhole) {
        final Rect2D rect = new Rect2D(
            SPACE.minX* spaceFraction,
            SPACE.minY* spaceFraction,
            SPACE.maxX* spaceFraction,
            SPACE.maxY* spaceFraction);

        for(Node node : store.getSpatialIndex().getApproximateNodesInArea(rect)) {
            blackhole.consume(node);
        }
        blackhole.consume(rect);
        return store;
    }

    @Benchmark
    @Measurement(iterations = 3)
    @Warmup(iterations = 1)
    @BenchmarkMode(Mode.AverageTime)
    public GraphStore nodesApproximateInAreaWithPredicateClassicIterator(Blackhole blackhole) {
        final Rect2D rect = new Rect2D(
            SPACE.minX* spaceFraction,
            SPACE.minY* spaceFraction,
            SPACE.maxX* spaceFraction,
            SPACE.maxY* spaceFraction);
        SpatialIndexImpl spatialIndex = (SpatialIndexImpl) store.getSpatialIndex();

        for(Node node : spatialIndex.getApproximateNodesInArea(rect, (node) -> node.getStoreId() % 2 == 0)) {
            blackhole.consume(node);
        }
        blackhole.consume(rect);
        blackhole.consume(spatialIndex);
        return store;
    }


    @Benchmark
    @Measurement(iterations = 3)
    @Warmup(iterations = 1)
    @BenchmarkMode(Mode.AverageTime)
    public GraphStore nodesInAreaWithoutPredicateSpliterator(Blackhole blackhole) {
        final Rect2D rect = new Rect2D(
            SPACE.minX* spaceFraction,
            SPACE.minY* spaceFraction,
            SPACE.maxX* spaceFraction,
            SPACE.maxY* spaceFraction);
        store.getSpatialIndex().getNodesInArea(rect).stream().forEach(blackhole::consume);
        blackhole.consume(rect);
        return store;
    }

    @Benchmark
    @Measurement(iterations = 3)
    @Warmup(iterations = 1)
    @BenchmarkMode(Mode.AverageTime)
    public GraphStore nodesInAreaWithPredicateSpliterator(Blackhole blackhole) {
        final Rect2D rect = new Rect2D(
            SPACE.minX* spaceFraction,
            SPACE.minY* spaceFraction,
            SPACE.maxX* spaceFraction,
            SPACE.maxY* spaceFraction);
        SpatialIndexImpl spatialIndex = (SpatialIndexImpl) store.getSpatialIndex();

        spatialIndex.getNodesInArea(rect, (node) -> node.getStoreId() % 2 == 0).stream().forEach(blackhole::consume);
        blackhole.consume(rect);
        blackhole.consume(spatialIndex);
        return store;
    }

    @Benchmark
    @Measurement(iterations = 3)
    @Warmup(iterations = 1)
    @BenchmarkMode(Mode.AverageTime)
    public GraphStore nodesInAreaWithoutPredicateSpliteratorParallel(Blackhole blackhole) {
        final Rect2D rect = new Rect2D(
            SPACE.minX* spaceFraction,
            SPACE.minY* spaceFraction,
            SPACE.maxX* spaceFraction,
            SPACE.maxY* spaceFraction);
        store.getSpatialIndex().getNodesInArea(rect).parallelStream().forEach(blackhole::consume);
        blackhole.consume(rect);
        return store;
    }

    @Benchmark
    @Measurement(iterations = 3)
    @Warmup(iterations = 1)
    @BenchmarkMode(Mode.AverageTime)
    public GraphStore nodesInAreaWithPredicateSpliteratorParallel(Blackhole blackhole) {
        final Rect2D rect = new Rect2D(
            SPACE.minX* spaceFraction,
            SPACE.minY* spaceFraction,
            SPACE.maxX* spaceFraction,
            SPACE.maxY* spaceFraction);
        SpatialIndexImpl spatialIndex = (SpatialIndexImpl) store.getSpatialIndex();
        spatialIndex.getNodesInArea(rect, (node) -> node.getStoreId() % 2 == 0).parallelStream().forEach(blackhole::consume);
        blackhole.consume(rect);
        return store;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
            .include(SpatialIndexNodeViewBenchmark.class.getSimpleName())
            .build();

        new Runner(opt).run();
    }
}
