package assignment3DAA;
import java.util.Arrays;
public class Tests {
    public static void main(String[] args) {
        testMST();
        testDisconnectedGraph();
        testSingleVertex();
        testMultipleMST();
        testSCC();
        testDijkstra();
        testUnreachableVertex();
    }

    public static void testMST() {
        Graph graph = new Graph(4, false);
        graph.addEdge(0, 1, 3);
        graph.addEdge(0, 2, 1);
        graph.addEdge(1, 2, 2);
        graph.addEdge(2, 3, 5);

        Kruskal kruskal = new Kruskal();
        kruskal.run(graph);

        Prim prim = new Prim();
        prim.run(graph);

        if (kruskal.totalWeight == 8 && prim.totalWeight == 8 && kruskal.connected && prim.connected) {
            System.out.println("MST: PASSED");
        } else {
            System.out.println("MST: FAILED");
        }
    }

    public static void testDisconnectedGraph() {
        Graph graph = new Graph(4, false);
        graph.addEdge(0, 1, 2);
        graph.addEdge(2, 3, 4);

        Kruskal kruskal = new Kruskal();
        kruskal.run(graph);
        Prim prim = new Prim();
        prim.run(graph);

        if (!kruskal.connected && !prim.connected && kruskal.mst.size() == 2 && prim.mst.size() == 1) {
            System.out.println("Disconnected graph: PASSED");
        } else {
            System.out.println("Disconnected graph: FAILED");
        }
    }

    public static void testSingleVertex() {
        Graph graph = new Graph(1, false);

        Kruskal kruskal = new Kruskal();
        kruskal.run(graph);
        Prim prim = new Prim();
        prim.run(graph);

        if (kruskal.connected && prim.connected && kruskal.totalWeight == 0 && prim.totalWeight == 0 && kruskal.mst.size() == 0 && prim.mst.size() == 0) {
            System.out.println("Single vertex: PASSED");
        } else {
            System.out.println("Single vertex: FAILED");
        }
    }

    public static void testMultipleMST() {
        Graph graph = new Graph(4, false);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 3, 1);
        graph.addEdge(3, 0, 1);
        graph.addEdge(0, 2, 2);

        Kruskal kruskal = new Kruskal();
        kruskal.run(graph);
        Prim prim = new Prim();
        prim.run(graph);

        if (kruskal.connected && prim.connected && kruskal.totalWeight == 3 && prim.totalWeight == 3 && kruskal.mst.size() == 3 && prim.mst.size() == 3) {
            System.out.println("Multiple MST: PASSED");
        } else {
            System.out.println("Multiple MST: FAILED");
        }
    }

    public static void testTopologicalSort() {
        Graph graph = new Graph(4, true);
        graph.addEdge(0, 1, 1);
        graph.addEdge(0, 2, 1);
        graph.addEdge(1, 3, 1);
        graph.addEdge(2, 3, 1);

        TopologicalSort topo = new TopologicalSort();
        topo.run(graph);

        boolean valid = !topo.hasCycle && topo.order.size() == graph.n;
        int[] position = new int[graph.n];

        for (int i = 0; i < topo.order.size(); i++) {
            position[topo.order.get(i)] = i;
        }

        for (Edge e : graph.edges) {
            if (position[e.u] >= position[e.v]) {
                valid = false;
                break;
            }
        }

        if (valid) {
            System.out.println("Topological Sort DAG: PASSED");
        } else {
            System.out.println("Topological Sort DAG: FAILED");
        }
    }

    public static void testTopologicalCycle() {
        Graph graph = new Graph(3, true);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 0, 1);

        TopologicalSort topo = new TopologicalSort();
        topo.run(graph);

        if (topo.hasCycle && topo.order.isEmpty()) {
            System.out.println("Topological Sort cycle: PASSED");
        } else {
            System.out.println("Topological Sort cycle: FAILED");
        }
    }


    public static void testSCC() {
        Graph graph = new Graph(6, true);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 0, 1);
        graph.addEdge(2, 3, 1);
        graph.addEdge(3, 4, 1);
        graph.addEdge(4, 3, 1);
        graph.addEdge(4, 5, 1);

        Kosaraju kosaraju = new Kosaraju();
        kosaraju.run(graph);

        boolean valid = kosaraju.components.size() == 3;
        int[] groupOf = new int[graph.n];

        for (int i = 0; i < graph.n; i++) {
            groupOf[i] = -1;
        }

        for (int i = 0; i < kosaraju.components.size(); i++) {
            for (int v : kosaraju.components.get(i)) {
                if (v < 0 || v >= graph.n || groupOf[v] != -1) {
                    valid = false;
                } else {
                    groupOf[v] = i;
                }
            }
        }

        for (int i = 0; i < graph.n; i++) {
            if (groupOf[i] == -1) {
                valid = false;
            }
        }

        if (valid && groupOf[0] == groupOf[1] && groupOf[1] == groupOf[2] && groupOf[3] == groupOf[4] && groupOf[0] != groupOf[3] && groupOf[0] != groupOf[5] && groupOf[3] != groupOf[5]) {
            System.out.println("SCC: PASSED");
        } else {
            System.out.println("SCC: FAILED");
        }
    }


    public static void testDijkstra() {
        Graph graph = new Graph(4, true);
        graph.addEdge(0, 1, 4);
        graph.addEdge(0, 2, 1);
        graph.addEdge(2, 1, 2);
        graph.addEdge(1, 3, 1);
        graph.addEdge(2, 3, 6);

        Dijkstra dijkstra = new Dijkstra();
        dijkstra.run(graph, 0);

        if (dijkstra.distance[0] == 0 && dijkstra.distance[1] == 3 && dijkstra.distance[2] == 1 && dijkstra.distance[3] == 4 && dijkstra.getPath(3).equals(Arrays.asList(0, 2, 1, 3))) {
            System.out.println("Dijkstra: PASSED");
        } else {
            System.out.println("Dijkstra: FAILED");
        }
    }


    public static void testUnreachableVertex() {
        Graph graph = new Graph(4, true);
        graph.addEdge(0, 1, 4);
        graph.addEdge(1, 2, 2);
        graph.addEdge(3, 1, 1);

        Dijkstra dijkstra = new Dijkstra();
        dijkstra.run(graph, 0);

        if (dijkstra.distance[0] == 0 && dijkstra.distance[1] == 4 && dijkstra.distance[2] == 6 && dijkstra.distance[3] == Long.MAX_VALUE && dijkstra.getPath(3).isEmpty()) {
            System.out.println("Unreachable vertex: PASSED");
        } else {
            System.out.println("Unreachable vertex: FAILED");
        }
    }


}
