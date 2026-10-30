package shortestpath;

import graph.Edge;
import graph.Graph;
import java.util.Arrays;
import java.util.PriorityQueue;

public final class Dijkstra {
    private Dijkstra() {}

    public static DijkstraResult run(Graph g, int source) {
        int n = g.V();
        if (source < 0 || source >= n) throw new IllegalArgumentException("bad source");
        for (Edge e : g.edges())
            if (e.weight < 0) throw new IllegalArgumentException("Dijkstra requires non-negative weights, found " + e);

        long[] dist = new long[n];
        int[] prev = new int[n];
        Arrays.fill(dist, DijkstraResult.INF);
        Arrays.fill(prev, -1);
        dist[source] = 0;

        final long[] cmp = {0};
        PriorityQueue<long[]> pq = new PriorityQueue<>((a, b) -> {
            cmp[0]++;
            return Long.compare(a[0], b[0]);
        });
        pq.add(new long[]{0, source});
        long pushes = 1, polls = 0, relax = 0, okRelax = 0;
        boolean[] done = new boolean[n];

        while (!pq.isEmpty()) {
            long[] top = pq.poll();
            polls++;
            int u = (int) top[1];
            if (done[u]) continue; 
            done[u] = true;
            for (Edge e : g.neighbors(u)) {
                relax++;
                long nd = dist[u] + e.weight;
                if (nd < dist[e.to]) {
                    dist[e.to] = nd;
                    prev[e.to] = u;
                    pq.add(new long[]{nd, e.to});
                    pushes++;
                    okRelax++;
                }
            }
        }
        return new DijkstraResult(source, dist, prev, relax, okRelax, pushes, polls, cmp[0]);
    }
}
