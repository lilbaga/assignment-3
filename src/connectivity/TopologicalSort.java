package connectivity;

import graph.Edge;
import graph.Graph;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class TopologicalSort {
    private TopologicalSort() {}

    public static final class Result {
        public final boolean isDag;
        public final List<Integer> order;
        public final int processed;
        public final long edgeExaminations;

        Result(boolean isDag, List<Integer> order, int processed, long edgeExaminations) {
            this.isDag = isDag;
            this.order = order;
            this.processed = processed;
            this.edgeExaminations = edgeExaminations;
        }
    }

    public static Result run(Graph g) {
        if (!g.isDirected()) throw new IllegalArgumentException("topological order needs a directed graph");
        int n = g.V();
        int[] indeg = new int[n];
        long examined = 0;
        for (int u = 0; u < n; u++)
            for (Edge e : g.neighbors(u)) {
                indeg[e.to]++;
                examined++;
            }

        Deque<Integer> queue = new ArrayDeque<>();
        for (int v = 0; v < n; v++) if (indeg[v] == 0) queue.add(v);

        List<Integer> order = new ArrayList<>(n);
        while (!queue.isEmpty()) {
            int u = queue.poll();
            order.add(u);
            for (Edge e : g.neighbors(u)) {
                examined++;
                if (--indeg[e.to] == 0) queue.add(e.to);
            }
        }
        boolean dag = order.size() == n;
        return new Result(dag, dag ? order : new ArrayList<>(), order.size(), examined);
    }

    public static boolean isValidOrder(Graph g, List<Integer> order) {
        int n = g.V();
        if (order.size() != n) return false;
        int[] pos = new int[n];
        java.util.Arrays.fill(pos, -1);
        for (int i = 0; i < n; i++) {
            int v = order.get(i);
            if (v < 0 || v >= n || pos[v] != -1) return false;
            pos[v] = i;
        }
        for (Edge e : g.edges()) if (pos[e.from] >= pos[e.to]) return false;
        return true;
    }
}
