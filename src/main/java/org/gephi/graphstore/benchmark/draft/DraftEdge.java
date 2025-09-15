package org.gephi.graphstore.benchmark.draft;

public class DraftEdge {

    private final int source;
    private final int target;
    private final int type;
    private final double weight;

    public DraftEdge(int source, int target, int type, double weight) {
        this.source = source;
        this.target = target;
        this.type = type;
        this.weight = weight;
    }

    public int getSource() {
        return source;
    }

    public int getTarget() {
        return target;
    }

    public int getType() {
        return type;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof DraftEdge draftEdge)) {
            return false;
        }

        return getSource() == draftEdge.getSource() && getTarget() == draftEdge.getTarget() && type == draftEdge.type;
    }

    @Override
    public int hashCode() {
        int result = getSource();
        result = 31 * result + getTarget();
        result = 31 * result + type;
        return result;
    }
}
