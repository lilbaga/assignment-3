package mst;

import graph.Edge;
import java.util.List;

public class MSTResult {
    public final List<Edge> edges;
    public final long totalWeight;
    public final boolean connected;
    public final int components;
    public final long edgeExaminations;
    public final long comparisons;
    public final long dataStructureOps;
    public final long dsuFinds;
    public final long dsuUnions;
    public final long pqPushes;
    public final long pqPolls;

    public MSTResult(List<Edge> edges, long totalWeight, boolean connected, int components,
                     long edgeExaminations, long comparisons,
                     long dsuFinds, long dsuUnions, long pqPushes, long pqPolls) {
        this.edges = edges;
        this.totalWeight = totalWeight;
        this.connected = connected;
        this.components = components;
        this.edgeExaminations = edgeExaminations;
        this.comparisons = comparisons;
        this.dsuFinds = dsuFinds;
        this.dsuUnions = dsuUnions;
        this.pqPushes = pqPushes;
        this.pqPolls = pqPolls;
        this.dataStructureOps = dsuFinds + dsuUnions + pqPushes + pqPolls;
    }
}
