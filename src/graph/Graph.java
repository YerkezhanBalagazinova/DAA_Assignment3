package assignment3DAA;
import java.util.ArrayList;
import java.util.List;
public class Graph {
    public int n;
    public boolean directed;
    public List<Edge> edges;
    public List<List<Edge>> adj;

    public Graph(int n, boolean directed) {
        this.n = n;
        this.directed = directed;
        edges = new ArrayList<>();
        adj = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
        }
    }

    public void addEdge(int u, int v, int w) {
        Edge e = new Edge(u, v, w);
        edges.add(e);
        adj.get(u).add(e);
        if (!directed) {
            adj.get(v).add(new Edge(v, u, w));
        }
    }
}



