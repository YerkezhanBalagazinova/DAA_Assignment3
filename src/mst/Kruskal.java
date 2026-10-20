package assignment3DAA;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Kruskal {
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

        List<Edge> sorted = new ArrayList<>(graph.edges);
        sorted.sort(Comparator.comparingInt(e -> e.w));

        UnionFind uf = new UnionFind(graph.n);

        for (Edge e : sorted) {
            edgeChecks++;
            if (uf.union(e.u, e.v)) {
                mst.add(e);
                totalWeight += e.w;

                if (mst.size() == graph.n - 1) {
                    break;
                }
            }
        }

        connected = mst.size() == graph.n - 1;
    }


}
