package graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public final class GraphGenerator {
    private GraphGenerator() {}

    public static Graph connectedUndirected(int n, int m, long seed, int maxWeight) {
        if (n < 1) throw new IllegalArgumentException("n >= 1 required");
        long maxEdges = (long) n * (n - 1) / 2;
        if (m < n - 1 || m > maxEdges)
            throw new IllegalArgumentException("m must be in [" + (n - 1) + ", " + maxEdges + "]");

        Random rnd = new Random(seed);
        Graph g = new Graph(n, false);
        Set<Long> used = new HashSet<>();

        List<Integer> perm = new ArrayList<>();
        for (int i = 0; i < n; i++) perm.add(i);
        Collections.shuffle(perm, rnd);
        for (int i = 1; i < n; i++) {
            int u = perm.get(i);
            int v = perm.get(rnd.nextInt(i)); 
            g.addEdge(u, v, 1 + rnd.nextInt(maxWeight));
            used.add(key(u, v, false, n));
        }
        addRandomEdges(g, used, m, rnd, maxWeight, false);
        return g;
    }

    public static Graph directedAcyclic(int n, int m, long seed, int maxWeight) {
        long maxEdges = (long) n * (n - 1) / 2;
        if (m < 0 || m > maxEdges) throw new IllegalArgumentException("m must be in [0, " + maxEdges + "]");
        Random rnd = new Random(seed);
        int[] rank = new int[n];
        List<Integer> perm = new ArrayList<>();
        for (int i = 0; i < n; i++) perm.add(i);
        Collections.shuffle(perm, rnd);
        for (int i = 0; i < n; i++) rank[perm.get(i)] = i;

        Graph g = new Graph(n, true);
        Set<Long> used = new HashSet<>();
        while (g.E() < m) {
            int a = rnd.nextInt(n), b = rnd.nextInt(n);
            if (a == b) continue;
            int u = rank[a] < rank[b] ? a : b;
            int v = u == a ? b : a;
            if (!used.add(key(u, v, true, n))) continue;
            g.addEdge(u, v, 1 + rnd.nextInt(maxWeight));
        }
        return g;
    }

    public static Graph randomDirected(int n, int m, long seed, int maxWeight) {
        long maxEdges = (long) n * (n - 1);
        if (m < 0 || m > maxEdges) throw new IllegalArgumentException("m must be in [0, " + maxEdges + "]");
        Random rnd = new Random(seed);
        Graph g = new Graph(n, true);
        addRandomEdges(g, new HashSet<>(), m, rnd, maxWeight, true);
        return g;
    }

    private static void addRandomEdges(Graph g, Set<Long> used, int m, Random rnd, int maxWeight, boolean directed) {
        int n = g.V();
        while (g.E() < m) {
            int u = rnd.nextInt(n), v = rnd.nextInt(n);
            if (u == v) continue;
            if (!used.add(key(u, v, directed, n))) continue;
            g.addEdge(u, v, 1 + rnd.nextInt(maxWeight));
        }
    }

    private static long key(int u, int v, boolean directed, int n) {
        if (!directed && u > v) { int t = u; u = v; v = t; }
        return (long) u * n + v;
    }
}
