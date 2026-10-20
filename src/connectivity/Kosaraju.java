package assignment3DAA;
import java.util.ArrayList;
import java.util.List;
public class Kosaraju {
    public List<List<Integer>> components = new ArrayList<>();
    public int edgeChecks;

    public void run(Graph graph) {
        if (!graph.directed) {
            throw new IllegalArgumentException("Graph must be directed");
        }

        components.clear();
        edgeChecks = 0;

        boolean[] visited = new boolean[graph.n];
        List<Integer> order = new ArrayList<>();

        for (int i = 0; i < graph.n; i++) {
            if (!visited[i]) {
                dfs1(i, graph, visited, order);
            }
        }

        Graph reversed = new Graph(graph.n, true);

        for (Edge e : graph.edges) {
            reversed.addEdge(e.v, e.u, e.w);
            edgeChecks++;
        }

        visited = new boolean[graph.n];

        for (int i = order.size() - 1; i >= 0; i--) {
            int v = order.get(i);
            if (!visited[v]) {
                List<Integer> group = new ArrayList<>();
                dfs2(v, reversed, visited, group);
                components.add(group);
            }
        }
    }

    private void dfs1(int v, Graph graph, boolean[] visited, List<Integer> order) {
        visited[v] = true;

        for (Edge e : graph.adj.get(v)) {
            edgeChecks++;
            if (!visited[e.v]) {
                dfs1(e.v, graph, visited, order);
            }
        }

        order.add(v);
    }

    private void dfs2(int v, Graph graph, boolean[] visited, List<Integer> group) {
        visited[v] = true;
        group.add(v);

        for (Edge e : graph.adj.get(v)) {
            edgeChecks++;
            if (!visited[e.v]) {
                dfs2(e.v, graph, visited, group);
            }
        }
    }

}
