package org.gephi.graphstore.benchmark.util;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphFactory;
import org.gephi.graph.api.Node;
import org.gephi.graph.impl.GraphModelImpl;
import org.gephi.graph.impl.GraphStore;
import org.gephi.graph.impl.NodeImpl;
import org.gephi.graphstore.benchmark.draft.DraftEdge;
import org.gephi.graphstore.benchmark.draft.DraftNode;

public class Generator {

    public static final float MINIMUM_POSITION = -10000;
    public static final float MAXIMUM_POSITION = 10000;
    protected final DraftNode[] nodes;
    protected final DraftEdge[] edges;
    protected Configuration configuration;
    protected boolean withNodes = true;
    protected boolean withEdges = true;
    protected boolean withPositions = false;

    private Generator(int nodes, int edges, int types) {
        this.nodes = new DraftNode[nodes];
        for (int i = 0; i < nodes; i++) {
            this.nodes[i] = new DraftNode(i);
        }

        this.edges = generateRandomEdges(this.nodes, edges, types);
        this.configuration = defaultConfiguration();
    }

    public static Configuration defaultConfiguration() {
        return Configuration.builder().
            nodeIdType(Integer.class).
            edgeIdType(Integer.class).
            enableSpatialIndex(false).
            enableAutoLocking(false).build();
    }

    private static DraftEdge[] generateRandomEdges(DraftNode[] nodes, int edgeCount, int typeCount) {
        Random random = new Random();
        Set<DraftEdge> edgeSet = new HashSet<>();
        while (edgeSet.size() < edgeCount) {
            DraftNode first = nodes[random.nextInt(nodes.length)];
            DraftNode second = nodes[random.nextInt(nodes.length)];
            if (first != second) {
                int type = 0;
                if (typeCount > 1) {
                    type = random.nextInt(typeCount);
                }
                DraftEdge edge = new DraftEdge(first.getId(), second.getId(), type, 1.0);
                edgeSet.add(edge);
            }
        }
        return edgeSet.toArray(new DraftEdge[0]);
    }

    public static Generator generate(int nodes, int edges) {
        return new Generator(nodes, edges, 1);
    }

    public static Generator generate(int nodes, int edges, int types) {
        return new Generator(nodes, edges, types);
    }

    public Generator withOnlyNodes() {
        this.withEdges = false;
        return this;
    }

    public Generator withNothing() {
        this.withNodes = false;
        return this;
    }

    public Generator withConfiguration(Configuration config) {
        this.configuration = config;
        return this;
    }

    public Generator withPositions() {
        this.withPositions = true;
        return this;
    }

    public GraphStore build() {
        GraphModelImpl model = new GraphModelImpl(this.configuration);
        GraphFactory factory = model.factory();
        GraphStore store = model.getStore();

        if (!withNodes) {
            return store;
        }

        Random rand = new Random();
        for (DraftNode draftNode : nodes) {
            Node node = factory.newNode(draftNode.getId());
            if (withPositions) {
                float x = MINIMUM_POSITION + rand.nextFloat() * (MAXIMUM_POSITION - MINIMUM_POSITION);
                float y = MINIMUM_POSITION + rand.nextFloat() * (MAXIMUM_POSITION - MINIMUM_POSITION);
                node.setPosition(x, y);
            }
            store.addNode(node);
        }

        if (!withEdges) {
            return store;
        }

        for (DraftEdge draftEdge : edges) {
            NodeImpl source = store.getNodeByStoreId(draftEdge.getSource());
            NodeImpl target = store.getNodeByStoreId(draftEdge.getTarget());
            Edge edge = factory.newEdge(source, target, draftEdge.getType(), true);
            store.addEdge(edge);
        }

        return store;
    }

    public DraftNode[] getNodes() {
        return nodes;
    }

    public DraftEdge[] getEdges() {
        return edges;
    }
}
