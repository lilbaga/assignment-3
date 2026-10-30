import connectivity.SCC;
import connectivity.TopologicalSort;
import dsu.DSU;
import graph.Graph;
import graph.GraphGenerator;
import mst.Kruskal;
import mst.MSTResult;
import mst.Prim;
import shortestpath.Dijkstra;
import shortestpath.DijkstraResult;
import util.ChartWriter;
import util.Table;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

public class Main {
    static final long SEED = 42;          
    static final int MAX_WEIGHT = 100;    
    static final int RUNS = 5;            
    static final long WARMUP_MS = 300;    

    static final String[] NAMES = {"Small", "Medium", "Large"};
    static final int[] VS = {10, 100, 1000};
    static final int[] ES = {30, 500, 5000};

    static final class Timed<T> {
        final double avgNs;
        final T result;
        Timed(double avgNs, T result) { this.avgNs = avgNs; this.result = result; }
    }

    static <T> Timed<T> bench(Supplier<T> task) {
        T last;
        long warmEnd = System.nanoTime() + WARMUP_MS * 1_000_000L;
        do { last = task.get(); } while (System.nanoTime() < warmEnd);
        long total = 0;
        for (int i = 0; i < RUNS; i++) {
            long t0 = System.nanoTime();
            last = task.get();
            total += System.nanoTime() - t0;
        }
        return new Timed<>(total / (double) RUNS, last);
    }

    static String us(double ns) { return String.format(Locale.US, "%.2f", ns / 1000.0); }
    static String ms(double ns) { return String.format(Locale.US, "%.2f", ns / 1_000_000.0); }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        Path out = Paths.get(args.length > 0 ? args[0] : "results");
        Path tablesDir = out.resolve("tables");
        Path plotsDir = out.resolve("plots");
        StringBuilder all = new StringBuilder();

        double[] tKruskal = new double[3], tPrim = new double[3], tScc = new double[3],
                 tTopo = new double[3], tDijk = new double[3];

        Table mstTable = new Table("Table 1. Kruskal vs Prim (connected undirected graphs, 5 runs, average)",
                "Graph", "V", "E", "Algorithm", "Avg time (µs)", "Edge examinations", "Comparisons",
                "DSU ops (find+union)", "PQ ops (push+poll)", "MST weight", "Connected");
        for (int i = 0; i < 3; i++) {
            Graph g = GraphGenerator.connectedUndirected(VS[i], ES[i], SEED + i, MAX_WEIGHT);
            Timed<MSTResult> k = bench(() -> Kruskal.run(g));
            Timed<MSTResult> p = bench(() -> Prim.run(g));
            tKruskal[i] = k.avgNs / 1000.0;
            tPrim[i] = p.avgNs / 1000.0;
            MSTResult kr = k.result, pr = p.result;
            mstTable.addRow(NAMES[i], VS[i], ES[i], "Kruskal", us(k.avgNs), kr.edgeExaminations, kr.comparisons,
                    kr.dsuFinds + kr.dsuUnions, "-", kr.totalWeight, kr.connected);
            mstTable.addRow(NAMES[i], VS[i], ES[i], "Prim", us(p.avgNs), pr.edgeExaminations, pr.comparisons,
                    "-", pr.pqPushes + pr.pqPolls, pr.totalWeight, pr.connected);
            System.out.printf("[check] %s: Kruskal weight=%d, Prim weight=%d -> %s%n", NAMES[i],
                    kr.totalWeight, pr.totalWeight, kr.totalWeight == pr.totalWeight ? "EQUAL" : "MISMATCH!");
        }
        emit(mstTable, "table1_mst", tablesDir, all);

        boolean[][] variants = {{false, false}, {true, false}, {false, true}, {true, true}};
        String[] variantNames = {"naive (no optimizations)", "path compression only",
                                 "union by rank only", "path compression + union by rank"};

        Table dsuStress = new Table("Table 2. DSU stress test: n = 20000, union(i,i+1) for all i, then find(i) for all i",
                "DSU variant", "Avg time (ms)", "Parent-pointer hops in find", "find calls", "union calls");
        final int N_STRESS = 20000;
        for (int v = 0; v < variants.length; v++) {
            final boolean pc = variants[v][0], rk = variants[v][1];
            Timed<DSU> t = bench(() -> {
                DSU d = new DSU(N_STRESS, pc, rk);
                for (int i = 0; i + 1 < N_STRESS; i++) d.union(i, i + 1);
                for (int i = 0; i < N_STRESS; i++) d.find(i);
                return d;
            });
            dsuStress.addRow(variantNames[v], ms(t.avgNs), t.result.parentSteps(), t.result.findCalls(), t.result.unionCalls());
        }
        emit(dsuStress, "table2_dsu_stress", tablesDir, all);

        Table dsuKruskal = new Table("Table 3. Kruskal on the Large graph (V=1000, E=5000) with different DSU variants",
                "DSU variant", "Avg time (µs)", "Parent-pointer hops in find", "MST weight");
        Graph large = GraphGenerator.connectedUndirected(VS[2], ES[2], SEED + 2, MAX_WEIGHT);
        for (int v = 0; v < variants.length; v++) {
            final boolean pc = variants[v][0], rk = variants[v][1];
            Timed<MSTResult> t = bench(() -> Kruskal.run(large, new DSU(large.V(), pc, rk)));
            DSU probe = new DSU(large.V(), pc, rk);
            Kruskal.run(large, probe);
            dsuKruskal.addRow(variantNames[v], us(t.avgNs), probe.parentSteps(), t.result.totalWeight);
        }
        emit(dsuKruskal, "table3_dsu_kruskal", tablesDir, all);

        Table sccTable = new Table("Table 4. Kosaraju SCC on random directed graphs (cycles allowed)",
                "Graph", "V", "E", "Avg time (µs)", "Edge examinations", "# SCC", "Largest SCC", "Contains cycle");
        Table topoTable = new Table("Table 5. Topological ordering (Kahn): DAGs vs graphs with cycles",
                "Graph", "V", "E", "Input", "Avg time (µs)", "Edge examinations", "Is DAG", "Vertices ordered", "Order valid");
        Table dijkTable = new Table("Table 6. Dijkstra from source 0 on random directed graphs (weights 1..100)",
                "Graph", "V", "E", "Avg time (µs)", "Edge relaxations (examined)", "Successful relaxations",
                "PQ pushes", "PQ polls", "Comparisons", "Reachable vertices");

        Graph[] cyclic = new Graph[3];
        for (int i = 0; i < 3; i++) {
            Graph d = GraphGenerator.randomDirected(VS[i], ES[i], SEED + 10 + i, MAX_WEIGHT);
            Graph dag = GraphGenerator.directedAcyclic(VS[i], ES[i], SEED + 20 + i, MAX_WEIGHT);
            cyclic[i] = d;

            Timed<SCC.Result> s = bench(() -> SCC.run(d));
            tScc[i] = s.avgNs / 1000.0;
            sccTable.addRow(NAMES[i], VS[i], ES[i], us(s.avgNs), s.result.edgeExaminations, s.result.count(),
                    s.result.largest(), s.result.largest() > 1);

            Timed<TopologicalSort.Result> tDag = bench(() -> TopologicalSort.run(dag));
            tTopo[i] = tDag.avgNs / 1000.0;
            topoTable.addRow(NAMES[i], VS[i], ES[i], "DAG", us(tDag.avgNs), tDag.result.edgeExaminations,
                    tDag.result.isDag, tDag.result.processed, TopologicalSort.isValidOrder(dag, tDag.result.order));
            Timed<TopologicalSort.Result> tCyc = bench(() -> TopologicalSort.run(d));
            topoTable.addRow(NAMES[i], VS[i], ES[i], "with cycles", us(tCyc.avgNs), tCyc.result.edgeExaminations,
                    tCyc.result.isDag, tCyc.result.processed, "n/a (cycle detected)");

            Timed<DijkstraResult> dj = bench(() -> Dijkstra.run(d, 0));
            tDijk[i] = dj.avgNs / 1000.0;
            DijkstraResult r = dj.result;
            dijkTable.addRow(NAMES[i], VS[i], ES[i], us(dj.avgNs), r.edgeRelaxations, r.successfulRelaxations,
                    r.pqPushes, r.pqPolls, r.comparisons, r.reachableCount() + "/" + VS[i]);
        }
        emit(sccTable, "table4_scc", tablesDir, all);
        emit(topoTable, "table5_topological", tablesDir, all);
        emit(dijkTable, "table6_dijkstra", tablesDir, all);

        double[] xv = {VS[0], VS[1], VS[2]};
        double[] xe = {ES[0], ES[1], ES[2]};
        Map<String, double[]> series = new LinkedHashMap<>();
        series.put("Kruskal", tKruskal);
        series.put("Prim", tPrim);
        series.put("SCC (Kosaraju)", tScc);
        series.put("Topological sort (DAG)", tTopo);
        series.put("Dijkstra", tDijk);

        ChartWriter.lineChart(plotsDir.resolve("time_vs_vertices.png").toFile(), "Execution time vs number of vertices",
                "Vertices (V)", "Average time (µs)", xv, series, false);
        ChartWriter.lineChart(plotsDir.resolve("time_vs_vertices_log.png").toFile(), "Execution time vs number of vertices",
                "Vertices (V)", "Average time (µs)", xv, series, true);
        ChartWriter.lineChart(plotsDir.resolve("time_vs_edges.png").toFile(), "Execution time vs number of edges",
                "Edges (E)", "Average time (µs)", xe, series, false);
        ChartWriter.lineChart(plotsDir.resolve("time_vs_edges_log.png").toFile(), "Execution time vs number of edges",
                "Edges (E)", "Average time (µs)", xe, series, true);
        Map<String, double[]> kp = new LinkedHashMap<>();
        kp.put("Kruskal", tKruskal);
        kp.put("Prim", tPrim);
        ChartWriter.lineChart(plotsDir.resolve("kruskal_vs_prim.png").toFile(), "Kruskal vs Prim: time vs number of edges",
                "Edges (E)", "Average time (µs)", xe, kp, false);

        StringBuilder demo = new StringBuilder();
        Graph sm = GraphGenerator.connectedUndirected(VS[0], ES[0], SEED, MAX_WEIGHT);
        MSTResult km = Kruskal.run(sm), pm = Prim.run(sm);
        demo.append("=== MST on the Small graph (V=10, E=30) ===\n");
        demo.append("Connected: ").append(km.connected).append('\n');
        demo.append("Kruskal weight = ").append(km.totalWeight).append(", edges = ").append(km.edges).append('\n');
        demo.append("Prim    weight = ").append(pm.totalWeight).append(", edges = ").append(pm.edges).append('\n');
        demo.append("Same weight: ").append(km.totalWeight == pm.totalWeight).append("\n\n");

        Graph smDag = GraphGenerator.directedAcyclic(VS[0], ES[0], SEED + 20, MAX_WEIGHT);
        TopologicalSort.Result tr = TopologicalSort.run(smDag);
        demo.append("=== Topological order of the Small DAG ===\nIs DAG: ").append(tr.isDag)
            .append("\nOrder: ").append(tr.order).append("\n\n");
        TopologicalSort.Result trc = TopologicalSort.run(cyclic[0]);
        demo.append("=== Topological order of the Small graph with cycles ===\nIs DAG: ").append(trc.isDag)
            .append(" (ordering impossible, only ").append(trc.processed).append(" of ").append(VS[0])
            .append(" vertices could be ordered)\n\n");

        Graph demoDir = GraphGenerator.randomDirected(10, 14, SEED + 30, MAX_WEIGHT); 
        SCC.Result sr = SCC.run(demoDir);
        demo.append("=== SCCs of a sparse directed graph (V=10, E=14) ===\nCount: ").append(sr.count()).append('\n');
        for (int c = 0; c < sr.components.size(); c++) demo.append("  SCC ").append(c).append(": ").append(sr.components.get(c)).append('\n');
        demo.append('\n');

        DijkstraResult dr = Dijkstra.run(demoDir, 0);
        demo.append("=== Dijkstra from vertex 0 on the sparse directed graph (V=10, E=14) ===\n");
        for (int v = 0; v < VS[0]; v++) {
            demo.append("  to ").append(v).append(": ");
            if (dr.reachable(v)) demo.append("dist=").append(dr.dist[v]).append(", path=").append(dr.pathTo(v));
            else demo.append("unreachable");
            demo.append('\n');
        }
        System.out.println();
        System.out.println(demo);
        Files.createDirectories(out);
        Files.writeString(out.resolve("demo_output.txt"), demo.toString());
        Files.writeString(tablesDir.resolve("all_tables.md"), all.toString());
        System.out.println("Done. Tables -> " + tablesDir + ", plots -> " + plotsDir);
    }

    private static void emit(Table t, String name, Path dir, StringBuilder all) throws Exception {
        t.print();
        t.save(dir, name);
        all.append(t.toMarkdown()).append("\n");
    }
}
