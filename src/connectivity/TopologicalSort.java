package assignment3DAA;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public class TopologicalSort {
    public List<Integer> order = new ArrayList<>();
    public boolean hasCycle;
    public int edgeChecks;

    public void run(Graph graph) {
        if (!graph.directed) {
            throw new IllegalArgumentException("Graph must be directed");
        }

        order.clear();
        hasCycle = false;
        edgeChecks = 0;

        int[] indegree = new int[graph.n];

        for (Edge e : graph.edges) {
            indegree[e.v]++;
            edgeChecks++;
        }

        Queue<Integer> queue = new ArrayDeque<>();

        for (int i = 0; i < graph.n; i++) {
            if (indegree[i] == 0) {
                queue.add(i);
            }
        }

        while (!queue.isEmpty()) {
            int u = queue.poll();
            order.add(u);

            for (Edge e : graph.adj.get(u)) {
                indegree[e.v]--;
                edgeChecks++;

                if (indegree[e.v] == 0) {
                    queue.add(e.v);
                }
            }
        }

        hasCycle = order.size() != graph.n;

        if (hasCycle) {
            order.clear();
        }
    }
}
