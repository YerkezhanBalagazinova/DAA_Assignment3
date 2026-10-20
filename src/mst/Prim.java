package assignment3DAA;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
public class Prim {
    public List<Edge> mst = new ArrayList<>();
    public int totalWeight;
    public boolean connected;
    public int edgeChecks;

    public void run(Graph graph) {
        if (graph.directed) {
            throw new IllegalArgumentException("Graph must be undirected");
        }

        mst.clear();
        totalWeight = 0;
        edgeChecks = 0;

        boolean[] visited = new boolean[graph.n];
        PriorityQueue<Edge> queue = new PriorityQueue<>(Comparator.comparingInt(e -> e.w));

        visited[0] = true;
        queue.addAll(graph.adj.get(0));

        while (!queue.isEmpty() && mst.size() < graph.n - 1) {
            Edge e = queue.poll();
            edgeChecks++;

            if (visited[e.v]) {
                continue;
            }

            visited[e.v] = true;
            mst.add(e);
            totalWeight += e.w;

            for (Edge next : graph.adj.get(e.v)) {
                if (!visited[next.v]) {
                    queue.add(next);
                }
            }
        }

        connected = mst.size() == graph.n - 1;
    }

}
