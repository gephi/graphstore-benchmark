package org.gephi.graphstore.benchmark;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.impl.GraphStoreConfiguration;
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
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * Measures {@code GraphModel.Serialization} write/read speed on graphs sized
 * to fall on either side of GraphStore's internal NodeStore/EdgeStore block
 * boundaries ({@code NODESTORE_BLOCK_SIZE} = 8192 nodes,
 * {@code EDGESTORE_BLOCK_SIZE} = 32768 edges), so a graph that needs more than
 * one block ("chunk") can be compared against one that fits in a single
 * block.
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(value = 3, warmups = 1, jvmArgs = { "-Xmx16g" })
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
public class SerializationBenchmark {

    private static final int BUFFER = 65536;

    /** {@code nodes:edges} */
    @Param({ "4000:8000", "20000:100000", "100000:500000"})
    public String workload;

    private GraphModel model;

    // Pre-serialized bytes for the deserialize benchmark, built once per trial.
    private byte[] serializedBytes;

    // Reused, pre-sized sink so the serialize benchmark never pays for array growth.
    private ByteArrayOutputStream sink;

    @Setup(Level.Trial)
    public void setUp() throws IOException {
        String[] parts = workload.split(":");
        int nodes = Integer.parseInt(parts[0]);
        int edges = Integer.parseInt(parts[1]);

        model = Generator.generate(nodes, edges).build().getModel();

        sink = new ByteArrayOutputStream(1 << 20);
        serializedBytes = write().toByteArray();
        sink = new ByteArrayOutputStream(serializedBytes.length + 1024);

        int nodeChunks = (int) Math.ceil(nodes / (double) GraphStoreConfiguration.NODESTORE_BLOCK_SIZE);
        int edgeChunks = (int) Math.ceil(edges / (double) GraphStoreConfiguration.EDGESTORE_BLOCK_SIZE);
        System.out.println("### SIZES workload=" + workload + " nodes=" + model.getGraph().getNodeCount() + " edges="
                + model.getGraph().getEdgeCount() + " nodeChunks=" + nodeChunks + " edgeChunks=" + edgeChunks
                + " bytes=" + serializedBytes.length);
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        model = null;
        serializedBytes = null;
        sink = null;
    }

    @Benchmark
    public int serialize() throws IOException {
        return write().size();
    }

    @Benchmark
    public GraphModel deserialize() throws IOException {
        return GraphModel.Serialization
                .read(new DataInputStream(new BufferedInputStream(new ByteArrayInputStream(serializedBytes), BUFFER)));
    }

    private ByteArrayOutputStream write() throws IOException {
        sink.reset();
        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(sink, BUFFER));
        GraphModel.Serialization.write(dos, model);
        dos.flush();
        return sink;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder().include(SerializationBenchmark.class.getSimpleName()).build();
        new Runner(opt).run();
    }
}
