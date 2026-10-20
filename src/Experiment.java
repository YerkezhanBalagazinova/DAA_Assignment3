
package assignment3DAA;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Random;

public class Experiment {
    public static void main(String[] args) throws FileNotFoundException {
        int[] vertices = {10, 100, 1000};
        int[] edges = {30, 500, 5000};
        String[] names = {"Small", "Medium", "Large"};
        String[] algorithms = {"Kruskal", "Prim", "Topological DAG", "Topological Cycle", "Kosaraju", "Dijkstra"};
        PrintWriter file = new PrintWriter("results.csv");
        file.println("Graph,Vertices,Edges,Algorithm,AverageTimeNs,AverageOperations,DSUFindCalls,DSUUnionCalls,DijkstraComparisons,DijkstraQueuePolls");

        for (int i = 0; i < 3; i++) {
            int n = vertices[i], m = edges[i];
            Graph graph = generateGraph(n, m);
            Graph dag = new Graph(n, true);
            for (Edge e : graph.edges) {
                dag.addEdge(Math.min(e.u, e.v), Math.max(e.u, e.v), e.w);
            }
            Graph cyclic = new Graph(n, true);
            for (int j = 0; j < m - 1; j++) {
                Edge e = dag.edges.get(j);
                cyclic.addEdge(e.u, e.v, e.w);
            }
            cyclic.addEdge(n - 1, 0, 1);

            Kruskal kruskal = new Kruskal();
            Prim prim = new Prim();
            TopologicalSort topoDag = new TopologicalSort();
            TopologicalSort topoCycle = new TopologicalSort();
            Kosaraju kosaraju = new Kosaraju();
            Dijkstra dijkstra = new Dijkstra();

            kruskal.run(graph);
            prim.run(graph);
            topoDag.run(dag);
            topoCycle.run(cyclic);
            kosaraju.run(cyclic);
            dijkstra.run(dag, 0);

            long[] times = new long[6];
            long[] operations = new long[6];
            long totalFinds = 0, totalUnions = 0;
            long totalComparisons = 0, totalPolls = 0;

            for (int j = 0; j < 5; j++) {
                UnionFind.findCalls = 0;
                UnionFind.unionCalls = 0;
                long start = System.nanoTime();
                kruskal.run(graph);
                times[0] += System.nanoTime() - start;
                operations[0] += kruskal.edgeChecks;
                totalFinds += UnionFind.findCalls;
                totalUnions += UnionFind.unionCalls;

                start = System.nanoTime();
                prim.run(graph);
                times[1] += System.nanoTime() - start;
                operations[1] += prim.edgeChecks;

                start = System.nanoTime();
                topoDag.run(dag);
                times[2] += System.nanoTime() - start;
                operations[2] += topoDag.edgeChecks;

                start = System.nanoTime();
                topoCycle.run(cyclic);
                times[3] += System.nanoTime() - start;
                operations[3] += topoCycle.edgeChecks;

                start = System.nanoTime();
                kosaraju.run(cyclic);
                times[4] += System.nanoTime() - start;
                operations[4] += kosaraju.edgeChecks;

                start = System.nanoTime();
                dijkstra.run(dag, 0);
                times[5] += System.nanoTime() - start;
                operations[5] += dijkstra.edgeChecks;
                totalComparisons += dijkstra.comparisons;
                totalPolls += dijkstra.queuePolls;
            }

            if (!kruskal.connected || !prim.connected || kruskal.totalWeight != prim.totalWeight) {
                throw new IllegalStateException("MST results do not match");
            }
            if (topoDag.hasCycle || topoDag.order.size() != n || !topoCycle.hasCycle) {
                throw new IllegalStateException("Topological result is wrong");
            }

            System.out.println(names[i] + " graph: V=" + n + ", E=" + m);
            for (int j = 0; j < 6; j++) {
                double averageTime = times[j] / 5.0;
                double averageOperations = operations[j] / 5.0;
                double findCalls = j == 0 ? totalFinds / 5.0 : 0;
                double unionCalls = j == 0 ? totalUnions / 5.0 : 0;
                double comparisons = j == 5 ? totalComparisons / 5.0 : 0;
                double queuePolls = j == 5 ? totalPolls / 5.0 : 0;

                System.out.printf(Locale.US, "%s: %.0f ns, %.1f operations%n", algorithms[j], averageTime, averageOperations);
                file.printf(Locale.US, "%s,%d,%d,%s,%.0f,%.1f,%.1f,%.1f,%.1f,%.1f%n", names[i], n, m, algorithms[j], averageTime, averageOperations, findCalls, unionCalls, comparisons, queuePolls);
            }

            System.out.println("MST weight: " + kruskal.totalWeight);
            System.out.println("SCC count: " + kosaraju.components.size());
            System.out.println();
        }
        file.close();
        System.out.println("Results saved to results.csv");
    }

    public static Graph generateGraph(int n, int m) {
        Graph graph = new Graph(n, false);
        Random random = new Random(42);
        boolean[][] exists = new boolean[n][n];

        for (int i = 1; i < n; i++) {
            int v = random.nextInt(i);
            graph.addEdge(i, v, random.nextInt(100) + 1);
            exists[i][v] = true;
            exists[v][i] = true;
        }

        while (graph.edges.size() < m) {
            int u = random.nextInt(n);
            int v = random.nextInt(n);
            if (u == v || exists[u][v]) {
                continue;
            }
            graph.addEdge(u, v, random.nextInt(100) + 1);
            exists[u][v] = true;
            exists[v][u] = true;
        }
        return graph;
    }
}
