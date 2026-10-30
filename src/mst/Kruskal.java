package mst;

import dsu.DSU;
import graph.Edge;
import graph.Graph;
import java.util.ArrayList;
import java.util.List;

public final class Kruskal {
    private Kruskal() {}

    public static MSTResult run(Graph g) {
        return run(g, new DSU(g.V()));
    }

    public static MSTResult run(Graph g, DSU dsu) {
        if (g.isDirected()) throw new IllegalArgumentException("MST needs an undirected graph");
        int n = g.V();

        List<Edge> sorted = new ArrayList<>(g.edges());
        final long[] cmp = {0};
        sorted.sort((a, b) -> {
            cmp[0]++;
            return Integer.compare(a.weight, b.weight);
        });

        List<Edge> mst = new ArrayList<>();
        long total = 0;
        long examined = 0;
        for (Edge e : sorted) {
            examined++;
            if (dsu.union(e.from, e.to)) {
                mst.add(e);
                total += e.weight;
                if (mst.size() == n - 1) break; 
            }
        }
        return new MSTResult(mst, total, dsu.components() <= 1, dsu.components(),
                examined, cmp[0], dsu.findCalls(), dsu.unionCalls(), 0, 0);
    }
}
