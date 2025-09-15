package org.gephi.graphstore.benchmark.draft;

public class DraftNode {

    private final int id;

    public DraftNode(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof DraftNode draftNode)) {
            return false;
        }

        return getId() == draftNode.getId();
    }

    @Override
    public int hashCode() {
        return getId();
    }
}
