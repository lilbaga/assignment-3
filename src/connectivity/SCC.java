package connectivity;

import graph.Edge;
import graph.Graph;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

public final class SCC {
    private SCC() {}

    public static final class Result {
        public final List<List<Integer>> components;
        public final int[] componentOf;
        public final long edgeExaminations;

        Result(List<List<Integer>> components, int[] componentOf, long edgeExaminations) {
            this.components = components;
            this.componentOf = componentOf;
            this.edgeExaminations = edgeExaminations;
        }

        public int count() { return components.size(); }

        public int largest() {
            int best = 0;
            for (List<Integer> c : components) best = Math.max(best, c.size());
            return best;
        }
    }

    public static Result run(Graph g) {
        if (!g.isDirected()) throw new IllegalArgumentException("SCC needs a directed graph");
        int n = g.V();
        long examined = 0;

        boolean[] visited = new boolean[n];
        int[] finish = new int[n];
        int fi = 0;
        int[] ptr = new int[n];
        Deque<Integer> stack = new ArrayDeque<>();
        for (int s = 0; s < n; s++) {
            if (visited[s]) continue;
            visited[s] = true;
            stack.push(s);
            while (!stack.isEmpty()) {
                int u = stack.peek();
                List<Edge> adj = g.neighbors(u);
                if (ptr[u] < adj.size()) {
                    Edge e = adj.get(ptr[u]++);
                    examined++;
                    if (!visited[e.to]) {
                        visited[e.to] = true;
                        stack.push(e.to);
                    }
                } else {
                    stack.pop();
                    finish[fi++] = u;
                }
            }
        }

        Graph rev = g.reverse();
        int[] comp = new int[n];
        java.util.Arrays.fill(comp, -1);
        List<List<Integer>> components = new ArrayList<>();
        for (int i = n - 1; i >= 0; i--) {
            int s = finish[i];
            if (comp[s] != -1) continue;
            int id = components.size();
            List<Integer> members = new ArrayList<>();
            comp[s] = id;
            stack.push(s);
            while (!stack.isEmpty()) {
                int u = stack.pop();
                members.add(u);
                for (Edge e : rev.neighbors(u)) {
                    examined++;
                    if (comp[e.to] == -1) {
                        comp[e.to] = id;
                        stack.push(e.to);
                    }
                }
            }
            Collections.sort(members);
            components.add(members);
        }
        return new Result(components, comp, examined);
    }
}
