
package assignment3DAA;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class Dijkstra {
    public long[] distance;
    public int[] previous;
    public int edgeChecks;
    public int comparisons;
    public int queuePolls;

    public void run(Graph graph, int start) {
        if (start < 0 || start >= graph.n) {
            throw new IllegalArgumentException("Invalid start vertex");
        }
        for (Edge e : graph.edges) {
            if (e.w < 0) {
                throw new IllegalArgumentException("Negative weights are not allowed");
            }
        }
        distance = new long[graph.n];
        previous = new int[graph.n];
        boolean[] visited = new boolean[graph.n];
        Arrays.fill(distance, Long.MAX_VALUE);
        Arrays.fill(previous, -1);
        edgeChecks = 0;
        comparisons = 0;
        queuePolls = 0;
        PriorityQueue<long[]> queue = new PriorityQueue<>(Comparator.comparingLong(a -> a[1]));
        distance[start] = 0;
        queue.add(new long[]{start, 0});
        while (!queue.isEmpty()) {
            long[] current = queue.poll();
            queuePolls++;
            int u = (int) current[0];
            if (visited[u]) {
                continue;
            }
            visited[u] = true;
            for (Edge e : graph.adj.get(u)) {
                edgeChecks++;
                long newDistance = distance[u] + e.w;
                comparisons++;
                if (newDistance < distance[e.v]) {
                    distance[e.v] = newDistance;
                    previous[e.v] = u;
                    queue.add(new long[]{e.v, newDistance});
                }
            }
        }
    }

    public List<Integer> getPath(int target) {
        List<Integer> path = new ArrayList<>();
        if (distance[target] == Long.MAX_VALUE) {
            return path;
        }
        for (int v = target; v != -1; v = previous[v]) {
            path.add(v);
        }
        Collections.reverse(path);
        return path;
    }
}
