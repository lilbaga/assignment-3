package shortestpath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DijkstraResult {
    public static final long INF = Long.MAX_VALUE;

    public final int source;
    public final long[] dist;
    public final int[] prev;
    public final long edgeRelaxations;   
    public final long successfulRelaxations;
    public final long pqPushes;
    public final long pqPolls;
    public final long comparisons;

    public DijkstraResult(int source, long[] dist, int[] prev, long edgeRelaxations, long successfulRelaxations,
                          long pqPushes, long pqPolls, long comparisons) {
        this.source = source;
        this.dist = dist;
        this.prev = prev;
        this.edgeRelaxations = edgeRelaxations;
        this.successfulRelaxations = successfulRelaxations;
        this.pqPushes = pqPushes;
        this.pqPolls = pqPolls;
        this.comparisons = comparisons;
    }

    public boolean reachable(int v) { return dist[v] != INF; }

    public int reachableCount() {
        int c = 0;
        for (long d : dist) if (d != INF) c++;
        return c;
    }

    public List<Integer> pathTo(int target) {
        if (!reachable(target)) return new ArrayList<>();
        List<Integer> path = new ArrayList<>();
        for (int v = target; v != -1; v = prev[v]) path.add(v);
        Collections.reverse(path);
        return path;
    }
}
