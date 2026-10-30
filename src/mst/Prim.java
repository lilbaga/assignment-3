package mst;

import graph.Edge;
import graph.Graph;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public final class Prim {
    private Prim() {}

    public static MSTResult run(Graph g) {
        if (g.isDirected()) throw new IllegalArgumentException("MST needs an undirected graph");
        int n = g.V();
        boolean[] inTree = new boolean[n];
        List<Edge> mst = new ArrayList<>();
        long total = 0, examined = 0, pushes = 0, polls = 0;
        int components = 0;

        final long[] cmp = {0};
        PriorityQueue<Edge> pq = new PriorityQueue<>((a, b) -> {
            cmp[0]++;
            return Integer.compare(a.weight, b.weight);
        });

        for (int start = 0; start < n; start++) {
            if (inTree[start]) continue;
            components++;
            inTree[start] = true;
            for (Edge e : g.neighbors(start)) {
                examined++;
                pq.add(e);
                pushes++;
            }
            while (!pq.isEmpty()) {
                Edge e = pq.poll();
                polls++;
                if (inTree[e.to]) continue; 
                inTree[e.to] = true;
                mst.add(e);
                total += e.weight;
                for (Edge f : g.neighbors(e.to)) {
                    examined++;
                    if (!inTree[f.to]) {
                        pq.add(f);
                        pushes++;
                    }
                }
            }
        }
        return new MSTResult(mst, total, components <= 1, components, examined, cmp[0], 0, 0, pushes, polls);
    }
}
