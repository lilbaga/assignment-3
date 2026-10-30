package tests;

import connectivity.SCC;
import connectivity.TopologicalSort;
import dsu.DSU;
import graph.Edge;
import graph.Graph;
import graph.GraphGenerator;
import mst.Kruskal;
import mst.MSTResult;
import mst.Prim;
import shortestpath.Dijkstra;
import shortestpath.DijkstraResult;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Tests {
    private static int passed = 0, failed = 0;

    private static void check(String name, boolean ok) {
        if (ok) { passed++; System.out.println("  [PASS] " + name); }
        else { failed++; System.out.println("  [FAIL] " + name); }
    }

    public static void main(String[] args) {
        testDsu();
        testMstSpecialCases();
        testMstRandom();
        testTopological();
        testScc();
        testDijkstra();
        System.out.println("\nPassed: " + passed + ", failed: " + failed);
        if (failed > 0) System.exit(1);
    }

    static void testDsu() {
        System.out.println("DSU");
        DSU d = new DSU(6);
        check("initially 6 components", d.components() == 6);
        check("union(0,1) merges", d.union(0, 1));
        check("union(1,0) again does nothing", !d.union(1, 0));
        d.union(2, 3);
        d.union(1, 2);
        check("0 and 3 connected", d.connected(0, 3));
        check("0 and 5 not connected", !d.connected(0, 5));
        check("components == 3", d.components() == 3);
        Random r = new Random(1);
        DSU[] ds = {new DSU(50, false, false), new DSU(50, true, false), new DSU(50, false, true), new DSU(50, true, true)};
        boolean same = true;
        for (int i = 0; i < 200; i++) {
            int a = r.nextInt(50), b = r.nextInt(50);
            boolean first = ds[0].union(a, b);
            for (int k = 1; k < 4; k++) same &= ds[k].union(a, b) == first;
        }
        check("all DSU variants agree on 200 random unions", same);
    }

    static void sameWeight(String name, Graph g, long expected, boolean connected) {
        MSTResult k = Kruskal.run(g), p = Prim.run(g);
        check(name + ": Kruskal weight = " + expected, k.totalWeight == expected);
        check(name + ": Prim weight = " + expected, p.totalWeight == expected);
        check(name + ": connected flag = " + connected, k.connected == connected && p.connected == connected);
    }

    static void testMstSpecialCases() {
        System.out.println("MST special cases");

        Graph small = new Graph(4, false); 
        small.addEdge(0, 1, 1); small.addEdge(1, 2, 2); small.addEdge(2, 3, 3);
        small.addEdge(0, 3, 10); small.addEdge(0, 2, 5);
        sameWeight("small graph", small, 6, true);

        Graph single = new Graph(1, false);
        sameWeight("single vertex", single, 0, true);
        check("single vertex: MST has no edges", Kruskal.run(single).edges.isEmpty() && Prim.run(single).edges.isEmpty());

        Graph disc = new Graph(5, false); 
        disc.addEdge(0, 1, 4); disc.addEdge(1, 2, 1); disc.addEdge(0, 2, 2); disc.addEdge(3, 4, 7);
        sameWeight("disconnected graph (forest)", disc, 1 + 2 + 7, false);
        check("disconnected: 2 components reported", Kruskal.run(disc).components == 2 && Prim.run(disc).components == 2);

        Graph cycle = new Graph(4, false); 
        cycle.addEdge(0, 1, 5); cycle.addEdge(1, 2, 5); cycle.addEdge(2, 3, 5); cycle.addEdge(3, 0, 5);
        sameWeight("equal weights / multiple valid MSTs", cycle, 15, true);
        check("MST of a cycle has V-1 edges", Kruskal.run(cycle).edges.size() == 3 && Prim.run(cycle).edges.size() == 3);

        Graph dup = new Graph(5, false); 
        dup.addEdge(0, 1, 2); dup.addEdge(1, 2, 2); dup.addEdge(2, 3, 2); dup.addEdge(3, 4, 2);
        dup.addEdge(0, 4, 2); dup.addEdge(0, 2, 2); dup.addEdge(1, 3, 3);
        sameWeight("duplicate weights", dup, 8, true);

        Graph none = new Graph(3, false); 
        sameWeight("no edges", none, 0, false);
    }

    static void testMstRandom() {
        System.out.println("MST on random connected graphs");
        boolean allEqual = true, allConnected = true, treeSize = true;
        for (int seed = 0; seed < 200; seed++) {
            int n = 2 + seed % 60;
            int maxE = n * (n - 1) / 2;
            int m = Math.min(maxE, n - 1 + (seed * 7) % (3 * n));
            Graph g = GraphGenerator.connectedUndirected(n, m, seed, 20); 
            MSTResult k = Kruskal.run(g), p = Prim.run(g);
            allEqual &= k.totalWeight == p.totalWeight;
            allConnected &= k.connected && p.connected;
            treeSize &= k.edges.size() == n - 1 && p.edges.size() == n - 1;
        }
        check("200 random graphs: Kruskal weight == Prim weight", allEqual);
        check("200 random graphs: generator output is connected", allConnected);
        check("200 random graphs: MST has exactly V-1 edges", treeSize);
    }

    static void testTopological() {
        System.out.println("Topological ordering");
        Graph dag = new Graph(6, true);
        dag.addEdge(5, 2, 1); dag.addEdge(5, 0, 1); dag.addEdge(4, 0, 1);
        dag.addEdge(4, 1, 1); dag.addEdge(2, 3, 1); dag.addEdge(3, 1, 1);
        TopologicalSort.Result r = TopologicalSort.run(dag);
        check("DAG detected", r.isDag);
        check("DAG order is valid", TopologicalSort.isValidOrder(dag, r.order));

        Graph cyc = new Graph(4, true);
        cyc.addEdge(0, 1, 1); cyc.addEdge(1, 2, 1); cyc.addEdge(2, 1, 1); cyc.addEdge(2, 3, 1);
        TopologicalSort.Result rc = TopologicalSort.run(cyc);
        check("cycle detected (no ordering)", !rc.isDag && rc.order.isEmpty());

        Graph self = new Graph(2, true);
        self.addEdge(0, 0, 1);
        check("self-loop is a cycle", !TopologicalSort.run(self).isDag);

        check("single vertex: order [0]", TopologicalSort.run(new Graph(1, true)).order.equals(List.of(0)));

        boolean ok = true, cycOk = true;
        for (int seed = 0; seed < 100; seed++) {
            int n = 5 + seed % 40;
            Graph g = GraphGenerator.directedAcyclic(n, Math.min(n * (n - 1) / 2, 2 * n), seed, 10);
            TopologicalSort.Result t = TopologicalSort.run(g);
            ok &= t.isDag && TopologicalSort.isValidOrder(g, t.order);
            if (!t.order.isEmpty() && g.E() > 0) {
                Edge e = g.edges().get(0);
                Graph h = new Graph(n, true);
                for (Edge x : g.edges()) h.addEdge(x.from, x.to, x.weight);
                h.addEdge(e.to, e.from, 1); 
                cycOk &= !TopologicalSort.run(h).isDag;
            }
        }
        check("100 random DAGs: valid topological order", ok);
        check("100 random DAGs + back edge: cycle detected", cycOk);
    }

    static void testScc() {
        System.out.println("Strongly connected components");
        Graph g = new Graph(8, true); // SCCs: {0,1,2}, {3,4}, {5,6}, {7}
        g.addEdge(0, 1, 1); g.addEdge(1, 2, 1); g.addEdge(2, 0, 1);
        g.addEdge(2, 3, 1); g.addEdge(3, 4, 1); g.addEdge(4, 3, 1);
        g.addEdge(4, 5, 1); g.addEdge(5, 6, 1); g.addEdge(6, 5, 1); g.addEdge(6, 7, 1);
        SCC.Result r = SCC.run(g);
        check("4 components found", r.count() == 4);
        check("{0,1,2} together", r.componentOf[0] == r.componentOf[1] && r.componentOf[1] == r.componentOf[2]);
        check("{3,4} together", r.componentOf[3] == r.componentOf[4]);
        check("{5,6} together", r.componentOf[5] == r.componentOf[6]);
        check("{0,1,2},{3,4},{5,6},{7} pairwise different",
                r.componentOf[0] != r.componentOf[3] && r.componentOf[3] != r.componentOf[5] && r.componentOf[5] != r.componentOf[7]);

        check("single vertex: 1 SCC", SCC.run(new Graph(1, true)).count() == 1);

        Graph dag = GraphGenerator.directedAcyclic(30, 60, 5, 10);
        check("DAG: every vertex is its own SCC", SCC.run(dag).count() == 30);

        Graph ring = new Graph(5, true);
        for (int i = 0; i < 5; i++) ring.addEdge(i, (i + 1) % 5, 1);
        check("directed ring: one SCC", SCC.run(ring).count() == 1);

        boolean partition = true, consistentWithTopo = true;
        for (int seed = 0; seed < 100; seed++) {
            int n = 3 + seed % 50;
            Graph rg = GraphGenerator.randomDirected(n, Math.min(n * (n - 1), n + seed % 60), seed, 10);
            SCC.Result sr = SCC.run(rg);
            int total = 0;
            boolean[] seen = new boolean[n];
            for (List<Integer> comp : sr.components) {
                total += comp.size();
                for (int v : comp) { partition &= !seen[v]; seen[v] = true; }
            }
            partition &= total == n;
            consistentWithTopo &= (sr.count() == n) == TopologicalSort.run(rg).isDag;
        }
        check("100 random graphs: every vertex in exactly one SCC", partition);
        check("100 random graphs: (#SCC == V) <=> topological sort succeeds", consistentWithTopo);
    }

    static void testDijkstra() {
        System.out.println("Dijkstra");
        Graph g = new Graph(6, true);
        g.addEdge(0, 1, 7); g.addEdge(0, 2, 9); g.addEdge(0, 5, 14);
        g.addEdge(1, 2, 10); g.addEdge(1, 3, 15); g.addEdge(2, 3, 11);
        g.addEdge(2, 5, 2); g.addEdge(3, 4, 6); g.addEdge(5, 4, 9);
        DijkstraResult r = Dijkstra.run(g, 0);
        check("dist to 4 is 20", r.dist[4] == 20);
        check("dist to 5 is 11", r.dist[5] == 11);
        check("path to 4 is 0-2-5-4", r.pathTo(4).equals(Arrays.asList(0, 2, 5, 4)));
        check("path to source is [0]", r.pathTo(0).equals(List.of(0)));

        Graph un = new Graph(4, true); 
        un.addEdge(0, 1, 1); un.addEdge(1, 2, 1);
        DijkstraResult ru = Dijkstra.run(un, 0);
        check("unreachable vertex has INF distance", !ru.reachable(3) && ru.dist[3] == DijkstraResult.INF);
        check("unreachable vertex has empty path", ru.pathTo(3).isEmpty());
        check("reachable count is 3", ru.reachableCount() == 3);

        check("single vertex: dist 0", Dijkstra.run(new Graph(1, true), 0).dist[0] == 0);

        Graph neg = new Graph(3, true);
        neg.addEdge(0, 1, 2); neg.addEdge(0, 2, 3); neg.addEdge(2, 1, -2);
        boolean threw = false;
        try { Dijkstra.run(neg, 0); } catch (IllegalArgumentException e) { threw = true; }
        check("negative weight rejected", threw);

        Graph zero = new Graph(3, true);
        zero.addEdge(0, 1, 0); zero.addEdge(1, 2, 0);
        check("zero-weight edges allowed", Dijkstra.run(zero, 0).dist[2] == 0);

        boolean ok = true;
        for (int seed = 0; seed < 100; seed++) {
            int n = 2 + seed % 40;
            Graph rg = GraphGenerator.randomDirected(n, Math.min(n * (n - 1), 2 * n), seed, 50);
            DijkstraResult dr = Dijkstra.run(rg, 0);
            long[] bf = bellmanFord(rg, 0);
            ok &= Arrays.equals(dr.dist, bf);
        }
        check("100 random graphs: Dijkstra == Bellman-Ford", ok);

        Graph ug = GraphGenerator.connectedUndirected(30, 80, 3, 20);
        DijkstraResult a = Dijkstra.run(ug, 0), b = Dijkstra.run(ug, 7);
        check("undirected: dist(0,7) == dist(7,0)", a.dist[7] == b.dist[0]);
        check("undirected connected: all reachable", a.reachableCount() == 30);
    }

    private static long[] bellmanFord(Graph g, int s) {
        long[] d = new long[g.V()];
        Arrays.fill(d, DijkstraResult.INF);
        d[s] = 0;
        for (int i = 0; i < g.V(); i++)
            for (Edge e : g.edges())
                if (d[e.from] != DijkstraResult.INF && d[e.from] + e.weight < d[e.to]) d[e.to] = d[e.from] + e.weight;
        return d;
    }
}
