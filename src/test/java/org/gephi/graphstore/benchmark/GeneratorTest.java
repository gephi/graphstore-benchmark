package org.gephi.graphstore.benchmark;


import org.gephi.graph.api.Node;
import org.gephi.graph.impl.GraphStore;
import org.gephi.graphstore.benchmark.util.Generator;
import org.junit.Test;

public class GeneratorTest {

    @Test
    public void testGeneratorNodes() {
        Generator generator = Generator.generate(100, 0);
        assert generator.getNodes().length == 100;
        assert generator.getEdges().length == 0;
        GraphStore store = generator.build();
        assert store.getNodeCount() == 100;
    }

    @Test
    public void testGeneratorOnlyNodes() {
        Generator generator = Generator.generate(100, 200).withOnlyNodes();
        assert generator.getNodes().length == 100;
        assert generator.getEdges().length == 200;
        GraphStore store = generator.build();
        assert store.getNodeCount() == 100;
    }

    @Test
    public void testGenerator() {
        Generator generator = Generator.generate(100, 200);
        GraphStore store = generator.build();
        assert store.getEdgeCount() == 200;
    }

    @Test
    public void testWithPositions() {
        Generator generator = Generator.generate(100, 200).withPositions();
        GraphStore store = generator.build();
        for (Node node : store.getNodes()) {
            float x = node.x();
            float y = node.y();
            assert x != 0;
            assert y != 0;
            assert x >= Generator.MINIMUM_POSITION && x <= Generator.MAXIMUM_POSITION;
            assert y >= Generator.MINIMUM_POSITION && y <= Generator.MAXIMUM_POSITION;
        }
    }
}
