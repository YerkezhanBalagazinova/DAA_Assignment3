
# Assignment 3 — Smart City Infrastructure and Route Planning

## 1. Overview

The goal of this assignment was to build a Java program that works with a city's infrastructure as a graph. A city has many districts, and these districts can be connected by roads, cables, or other infrastructure. If we represent these connections as a graph, we can use algorithms to answer questions such as: What is the cheapest way to connect all districts? Which locations can reach each other? What is the shortest route from one district to another?

In my program, **vertices** represent city districts or locations, **edges** represent connections between them, and **edge weights** represent costs or distances. The meaning of a weight depends on the task: for minimum spanning trees it can be a construction cost, while for Dijkstra it can be a travel distance.

I implemented the following algorithms:

- **Kruskal** and **Prim** to find a minimum spanning tree (MST).
- **Union-Find (DSU)** to track connected components while running Kruskal.
- **Topological Sort (Kahn's algorithm)** to order vertices in a directed acyclic graph and detect cycles.
- **Kosaraju** to find strongly connected components (SCCs) in a directed graph.
- **Dijkstra** to calculate shortest distances and reconstruct paths from a selected starting vertex.

I also added tests and experiments to check the results and compare execution time on small, medium, and large graphs. This is a console-based Java project, not an interactive city map. The city scenario is used to explain what each algorithm could solve in a real system.

### Project structure

```text
assignment-3/
├── src/
│   └── assignment3DAA/
│       ├── Edge.java
│       ├── Graph.java
│       ├── UnionFind.java
│       ├── Kruskal.java
│       ├── Prim.java
│       ├── TopologicalSort.java
│       ├── Kosaraju.java
│       ├── Dijkstra.java
│       ├── Tests.java
│       └── Experiment.java
├── results/
│   ├── tables/
│   │   └── results.csv
│   └── plots/
│       ├── execution_time_vs_vertices.png
│       └── execution_time_vs_edges.png
└── README.md
```



## 2. Implementation

### 2.1. Graph representation

I used an **adjacency list** because it lets the algorithms find the neighbors of a vertex without checking every possible vertex. The `Graph` class stores the number of vertices, a flag showing whether the graph is directed, a list of edges, and adjacency lists.

The `Edge` class stores three values: `u` (starting vertex), `v` (ending vertex), and `w` (weight). For an undirected graph, I add the reverse connection to the adjacency list as well. However, each undirected connection appears only once in the main `edges` list. This is useful for Kruskal because it should not sort the same undirected edge twice.

For example, the edge `0 -- 1` with weight 3 is available from both vertices in the adjacency lists. In a directed graph, `0 -> 1` does not automatically create `1 -> 0`.

### 2.2. Union-Find (Disjoint Set Union)

Union-Find is used by Kruskal to check whether two vertices already belong to the same connected group. I implemented two main methods:

- `find(x)` returns the root of the group containing vertex `x`.
- `union(a, b)` combines two groups if their roots are different.

I used **path compression** in `find()`. After finding a root, the method updates parent references, so the same vertices can be found faster later. I also used **union by size**: the smaller group is attached to the larger one. This helps prevent the DSU trees from becoming too deep.

For the experiments, I added counters for calls to `find()` and `union()`. The `find()` counter includes recursive calls. I reset the counters before measuring each Kruskal run.

### 2.3. Kruskal's algorithm

Kruskal finds a minimum spanning tree for an undirected weighted graph. It works by sorting all edges from the smallest weight to the largest. Then it goes through the sorted edges and adds an edge only if it connects two different DSU groups. If both ends are already in the same group, adding that edge would create a cycle, so it is skipped.

The algorithm stops when the MST contains `V - 1` edges, because that is how many edges a spanning tree needs for `V` vertices. It also calculates the total MST weight and checks whether all vertices were connected.

A small example from my tests uses these edges:

| Edge | Weight |
|---|---:|
| 0–1 | 3 |
| 0–2 | 1 |
| 1–2 | 2 |
| 2–3 | 5 |

The MST uses `0–2`, `1–2`, and `2–3`. Its total weight is **1 + 2 + 5 = 8**.

### 2.4. Prim's algorithm

Prim solves the same MST problem but uses a different approach. It starts from vertex `0`, marks it as visited, and puts its available edges into a Java `PriorityQueue`. At each step, it takes the cheapest edge that leads to a new vertex. After visiting that vertex, it adds its outgoing edges to the queue.

I used a `visited[]` array so that a vertex is not added to the MST twice. Like Kruskal, Prim records the chosen edges, total weight, and whether a complete spanning tree was found.

The important thing is that **Kruskal and Prim do not always select exactly the same edges**. When several edges have equal weights, more than one correct MST may exist. However, for a connected graph, both algorithms must return the same minimum total weight.

If a graph is disconnected, Kruskal produces a minimum spanning forest, while this Prim implementation only builds a tree for the component reachable from vertex `0`. In both cases, `connected` is `false`.

### 2.5. Topological Sort — Kahn's algorithm

Topological sorting is used for directed graphs where one action or location must come before another. For example, one infrastructure task might need to be completed before another task can start.

I implemented Kahn's algorithm. First, it counts the **indegree** of every vertex, which is the number of incoming edges. Vertices with indegree `0` are added to a queue. When a vertex is removed from the queue, it is added to the result, and the indegrees of its neighbors are decreased. Any neighbor whose indegree becomes `0` is added to the queue.

For a DAG containing `0 -> 1`, `0 -> 2`, `1 -> 3`, and `2 -> 3`, one valid order is `0, 1, 2, 3`. The order `0, 2, 1, 3` is also valid.

If the algorithm cannot process all `V` vertices, the graph contains a cycle. In this case, the program sets `hasCycle = true` and does not return a valid topological order.

### 2.6. Strongly Connected Components — Kosaraju's algorithm

A strongly connected component is a group of vertices where every vertex can reach every other vertex in the same group by following directed edges. In the city example, this could describe locations that are mutually reachable through one-way roads.

Kosaraju's algorithm uses two depth-first searches (DFS):

1. Run DFS on the original directed graph and record the order in which vertices finish.
2. Reverse the direction of every edge, then process vertices in reverse finishing order with another DFS.

Each group found by the second DFS is one SCC. For example, a test graph has three components: `{0, 1, 2}`, `{3, 4}`, and `{5}`.

I store the components as lists of vertices. The tests also check that every vertex appears in exactly one component.

### 2.7. Dijkstra's algorithm

Dijkstra finds the shortest distances from one starting vertex to all reachable vertices. I used a `PriorityQueue` that stores pairs of values: a vertex and its current distance. The queue lets the algorithm process the smallest available distance first.

The `distance[]` array stores the shortest distances found so far, while `previous[]` stores the previous vertex on the best known route. This makes it possible to reconstruct a full path using `getPath()`.

For example, a test graph produces these distances from vertex `0`:

| Vertex | Shortest distance from 0 |
|---|---:|
| 0 | 0 |
| 1 | 3 |
| 2 | 1 |
| 3 | 4 |

The shortest path to vertex `3` is **0 -> 2 -> 1 -> 3**, with total weight **1 + 2 + 1 = 4**.

If a vertex cannot be reached, its distance stays `Long.MAX_VALUE`, and `getPath()` returns an empty list. I used `long` for distances to reduce the risk of overflow when adding larger weights.

Dijkstra requires **non-negative edge weights**. The algorithm assumes that once the nearest unvisited vertex is selected, its shortest distance is final. A negative edge could create a shorter route later and break that assumption. For graphs with negative weights, Bellman-Ford is a more suitable algorithm. My implementation checks the input and rejects negative edge weights.

## 3. Complexity Analysis

In the following explanations, **V** is the number of vertices and **E** is the number of edges. I used adjacency lists and Java's priority queue, which is implemented as a binary heap. The space values below describe the extra memory used by the algorithm, excluding the input graph unless stated otherwise.

| Algorithm | Time complexity | Extra space | Explanation |
|---|---|---|---|
| DSU initialization | O(V) | O(V) | Creates the `parent[]` and `size[]` arrays. |
| DSU `find()` / `union()` | O(α(V)) amortized per operation | O(V) for DSU storage | Path compression and union by size make the operations almost constant time over a sequence of calls. |
| Kruskal | O(E log E + Eα(V)) | O(E + V) | Sorts all edges, then checks them with DSU. |
| Prim | O(E log E) | O(V + E) | Uses a binary-heap priority queue, which may contain multiple edge candidates. |
| Topological Sort (Kahn) | O(V + E) | O(V) | Calculates indegrees and processes vertices and edges. |
| Kosaraju (SCC) | O(V + E) | O(V + E) | Runs two DFS passes and builds a reversed graph. |
| Dijkstra | O((V + E) log(V + E)) | O(V + E) | Uses a binary heap with possible repeated entries for a vertex. |

Here, **α(V)** is the inverse Ackermann function. It grows so slowly that, for realistic graph sizes, DSU operations are very close to constant time.

A few details are worth mentioning:

- **DSU:** Without the two optimizations, its trees could become unnecessarily deep. Path compression shortens paths during searches, and union by size keeps new trees balanced.
- **Kruskal:** Sorting is usually the most expensive part, especially as the number of edges grows. Even if Kruskal stops examining edges after finding `V - 1` MST edges, it has already sorted the full list.
- **Prim:** The priority queue performs insertions and removals. A graph with more candidate edges can require more heap operations.
- **Topological Sort and Kosaraju:** Both grow linearly with the size of the graph representation, `V + E`.
- **Dijkstra:** Processing edges is not the only work. Updating the priority queue also takes time. My version does not decrease keys directly; it adds a new entry when a better distance is found and skips old entries later.

The adjacency-list representation itself needs **O(V + E)** space. In the experiment generator I also use a `boolean[][]` array to avoid duplicate edges; that temporary generator needs **O(V²)** space. Graph generation is **not** included in the measured algorithm times.

## 4. Experimental Setup

I tested all algorithms using the three graph sizes required in the assignment:

| Graph size | Vertices (V) | Edges (E) |
|---|---:|---:|
| Small | 10 | 30 |
| Medium | 100 | 500 |
| Large | 1,000 | 5,000 |

### 4.1. Graph generation

I used `Random(42)` so the graphs are reproducible. The edge weights are random integers from **1 to 100**. Self-loops and duplicate connections are not added to the generated undirected graphs.

For **Kruskal and Prim**, I first connect every new vertex to one of the earlier vertices. This creates a connected starting graph with `V - 1` edges. I then add random edges until the graph contains the required total `E`. This guarantees that the undirected graph is connected before the MST algorithms run.

For the **directed acyclic graph (DAG)**, I take the generated undirected edges and direct each one from the smaller vertex index to the larger vertex index. Because every edge goes toward a higher index, there cannot be a directed cycle. I use this graph for Topological Sort and Dijkstra.

For the **cyclic directed graph**, I copy all but one of the DAG edges and add an edge from vertex `V - 1` back to vertex `0`. In the three generated test graphs, this creates a cycle, which is confirmed by the topological sorting check. I use the cyclic graph for the cycle-detection experiment and Kosaraju.

Each directed graph also contains the required number of edges: 30, 500, or 5,000. All weights remain non-negative, which is necessary for Dijkstra.

### 4.2. Measuring performance

For each graph size, I generate the graph once. **All five measured runs of an algorithm use that same input graph**, rather than five different random graphs. This makes the runs easier to compare.

Before recording results, I execute each algorithm once as a simple warm-up. Then I run it **five more times** and measure each run using `System.nanoTime()`. I add the five measurements and divide by five to get the average. I do the same for the operation counters.

The timer only surrounds the algorithm call. It does not include generating the graph, writing the CSV file, or drawing the plots.

The program saves the results in `results.csv`. For the GitHub report, I keep a copy in [`results/tables/results.csv`](results/tables/results.csv). The CSV values come from my own program run.

### 4.3. What the operation counters mean

I used counters already available in the algorithm classes and added extra counters for Union-Find and Dijkstra:

| Algorithm | Main `AverageOperations` counter |
|---|---|
| Kruskal | Number of sorted edges examined before the MST is complete. |
| Prim | Number of edges removed from the priority queue (`poll`). |
| Topological Sort | Edge checks during indegree calculation and queue processing. |
| Kosaraju | Edge checks during DFS passes and creation of the reversed graph. |
| Dijkstra | Outgoing edges examined when processing reachable vertices. |

I also record `DSUFindCalls` and `DSUUnionCalls` for Kruskal, plus `DijkstraComparisons` (distance comparisons) and `DijkstraQueuePolls` (queue removals). The zero values in these special CSV columns mean that the corresponding metric is not collected for that algorithm.

These counters **do not measure exactly the same type of work**. For example, one Kruskal edge examination is not necessarily as expensive as one Prim priority-queue removal. Therefore, operation counts are useful for understanding each algorithm, but should not be treated as a universal measure of speed.

## 5. Results

The results below are taken from my `results.csv`. Execution time is shown in **milliseconds (ms)** to make the tables easier to read. The original CSV stores time in **nanoseconds (ns)**, where `1 ms = 1,000,000 ns`.

### 5.1. Average execution time

| Algorithm | Small: 10 V, 30 E | Medium: 100 V, 500 E | Large: 1,000 V, 5,000 E |
|---|---:|---:|---:|
| Kruskal | 0.04774 ms | 0.42862 ms | 1.42076 ms |
| Prim | 0.04864 ms | 0.23204 ms | 2.98370 ms |
| Topological Sort (DAG) | 0.02070 ms | 0.13954 ms | 0.37482 ms |
| Topological Sort (cycle) | 0.00412 ms | 0.03586 ms | 0.11026 ms |
| Kosaraju | 0.04636 ms | 0.16132 ms | 2.54326 ms |
| Dijkstra | 0.04342 ms | 0.17102 ms | 1.23548 ms |

### 5.2. Average operation counts

| Algorithm | Small | Medium | Large |
|---|---:|---:|---:|
| Kruskal — edges examined | 14 | 164 | 3,356 |
| Prim — priority-queue removals | 14 | 166 | 3,393 |
| Topological Sort (DAG) — edge checks | 60 | 1,000 | 10,000 |
| Topological Sort (cycle) — edge checks | 30 | 500 | 5,000 |
| Kosaraju — edge checks | 90 | 1,500 | 15,000 |
| Dijkstra — edge checks | 30 | 500 | 5,000 |

### 5.3. Additional DSU and Dijkstra measurements

| Graph | Kruskal: `find()` calls | Kruskal: `union()` calls | Dijkstra: distance comparisons | Dijkstra: queue removals |
|---|---:|---:|---:|---:|
| Small | 43 | 14 | 30 | 13 |
| Medium | 576 | 164 | 500 | 131 |
| Large | 12,916 | 3,356 | 5,000 | 1,267 |

The `find()` count is higher than the `union()` count because each union calls `find()` for both endpoints, and `find()` can make additional recursive calls. In Dijkstra, the number of queue removals can be greater than the number of vertices because the same vertex may have more than one entry in the priority queue.

For the fixed-seed undirected graphs, the MST total weights are **129 (Small)**, **1,170 (Medium)**, and **13,123 (Large)**. Kruskal and Prim return the same total weight for each graph, which is one of the correctness checks in the experiment.

### 5.4. Execution time vs. number of vertices

![Execution Time vs. Number of Vertices](results/plots/execution_time_vs_vertices.png)

This graph compares the average running time of all six experiment types as the number of vertices increases from 10 to 100 and then 1,000.

### 5.5. Execution time vs. number of edges

![Execution Time vs. Number of Edges](results/plots/execution_time_vs_edges.png)

This graph uses the same measured times but shows them against 30, 500, and 5,000 edges. In this experiment, vertices and edges increase together, so the two graphs show different views of the **same three graph configurations**. They do not measure the independent effect of changing only one variable.

The complete results, including the counters, are available in [`results/tables/results.csv`](results/tables/results.csv).

## 6. Comparison and Discussion

### 6.1. Kruskal vs. Prim

The two MST algorithms produced the same total weight on all three connected graphs, which is what we expect. Their running times, however, were different.

On the **Small** graph, the difference was very small: Kruskal took **0.04774 ms** and Prim took **0.04864 ms**. On the **Medium** graph, Prim was faster (**0.23204 ms** compared with **0.42862 ms**). On the **Large** graph, the result was reversed: Kruskal took **1.42076 ms**, while Prim took **2.98370 ms**.

This does not prove that Kruskal is always faster than Prim. Both use different data structures and do different amounts of work. Kruskal sorts the entire edge list and uses DSU to avoid cycles. Prim works with a priority queue and may add several possible edges before choosing one. Java's execution time can also change because of JVM warm-up, memory allocation, and background activity on the computer.

The edge-related counts were close in this experiment: on the Large graph, Kruskal examined **3,356** sorted edges while Prim removed **3,393** edges from its queue. These are different kinds of operations, so their values should not be compared as if they were identical instructions.

The DSU results also help explain Kruskal's work. On the Large graph there were **3,356** union attempts and **12,916** calls to `find()`, including recursion. Path compression and union by size make these calls efficient even when the graph grows.

### 6.2. Topological sorting on DAGs and cyclic graphs

Kahn's algorithm returned a valid order for the DAGs. For the cyclic graphs, it detected that a complete topological order was impossible.

An interesting result is that the cyclic graphs were faster in my measurements. For example, on the Large graph, the DAG took **0.37482 ms**, while the cyclic graph took **0.11026 ms**. This is because of the specific cyclic inputs I generated. Their vertices do not provide an initial vertex with indegree zero, so the queue is empty at the start. The algorithm detects the cycle without running the full queue-processing stage.

The operation counts support this explanation. On the DAG, the algorithm checks every edge when computing indegrees and again while processing vertices, so it records **2E** checks. On the cyclic graph, it only makes the first **E** checks in these measurements. This does **not** mean that a graph with cycles is always faster to analyze; the result depends on where the cycle is and how much of the graph Kahn's algorithm can process.

### 6.3. Kosaraju and SCC performance

Kosaraju's average running time increased from **0.04636 ms** on the Small graph to **0.16132 ms** on the Medium graph and **2.54326 ms** on the Large graph.

The measured edge checks were **90**, **1,500**, and **15,000**. They are exactly three times the corresponding number of edges because my counter includes the first DFS, creation of the reversed graph, and the second DFS. This matches the idea that Kosaraju performs a constant number of passes over the graph, giving it a theoretical complexity of **O(V + E)**.

The measured time does not increase at exactly the same rate as the edge count. Building extra lists, recursive DFS calls, and JVM behavior all affect the measured time. Still, the operation counts show that the amount of graph traversal work grows in a predictable way.

### 6.4. Dijkstra's performance

Dijkstra took **0.04342 ms**, **0.17102 ms**, and **1.23548 ms** on the Small, Medium, and Large DAGs. Its edge examinations and distance comparisons grew from **30** to **500** to **5,000**.

The priority queue was used more often as the graphs became larger: **13**, **131**, and **1,267** removals. This is because an initially discovered route can be improved later, causing another entry for the same vertex to be added to the queue. The older entry is ignored when it is removed if that vertex has already been visited.

The results are consistent with the algorithm having to examine more edges and do more priority-queue work on larger graphs. The important difference from an ordinary BFS is that Dijkstra considers the weights of edges, not just the number of edges in a route.

### 6.5. What the experiments show — and their limits

Overall, the results are reasonable compared with the theoretical complexity analysis: larger graphs require more work, linear graph traversals have counters proportional to `E`, and the heap-based algorithms spend additional time managing their queues.

However, I only tested **three graph sizes** and **one fixed graph per size**, with five timing repetitions. Five runs help reduce random timing fluctuations, but they are not the same as testing five different graphs. Also, increasing `V` and `E` at the same time means the experiments cannot separate their effects. For these reasons, I use the results to explain the behavior of my implementations, not to claim that one algorithm will always be fastest on every graph.

## 7. Testing and Correctness

I created a separate `Tests.java` class to check correctness using small graphs where the expected result can be calculated manually. I focused on the special cases mentioned in the assignment, not only on random performance tests.

| Test | Graph or input | What is checked |
|---|---|---|
| Basic MST | Four vertices with known edge weights | Kruskal and Prim both find an MST of total weight **8**, with three edges. |
| Disconnected graph | Two separate pairs of connected vertices | Both algorithms report `connected = false`; Kruskal returns a forest and Prim returns the tree reachable from vertex 0. |
| Single vertex | One vertex and no edges | MST weight is **0**, no edges are needed, and the graph is connected. |
| Multiple valid MSTs / duplicate weights | A square whose edges have equal weight | Different MSTs are possible, but the minimum total weight is **3**. |
| Topological Sort on a DAG | Directed graph without cycles | For every edge `u -> v`, vertex `u` appears before `v` in the returned order. |
| Topological Sort with a cycle | `0 -> 1 -> 2 -> 0` | `hasCycle` is true and no valid order is returned. |
| SCC | Graph with SCCs `{0,1,2}`, `{3,4}`, `{5}` | All three groups are found and every vertex belongs to exactly one SCC. |
| Dijkstra shortest paths | Directed weighted graph with known routes | Distances from 0 are **0, 3, 1, 4** and the path to vertex 3 is **0 -> 2 -> 1 -> 3**. |
| Unreachable vertex | Vertex that cannot be reached from the source | Its distance remains `Long.MAX_VALUE`, and its path is empty. |

The main comparison for the MST is the **total weight**, not the exact list of edges, because equal weights can produce different correct trees. For topological sorting, I verify the relative order of connected vertices rather than assuming that only one valid ordering exists.

The experiment also includes automatic checks: Kruskal and Prim must both report a connected MST with the same total weight, the generated DAG must not contain a cycle, and the generated cyclic graph must be detected as cyclic. If one of these checks fails, `Experiment.java` throws an exception rather than silently reporting the measurements.

I also validate the input for Dijkstra. If an edge has a negative weight, the program throws an `IllegalArgumentException` instead of running an algorithm that does not support that input.

## 8. How to Run the Project

I used Java and IntelliJ IDEA for this project. There are no additional libraries or dependencies; the algorithms use standard Java classes such as `ArrayList`, `Queue`, `PriorityQueue`, and `Random`.

1. Open the project folder in IntelliJ IDEA.
2. Make sure all Java files are inside `src/assignment3DAA` and have `package assignment3DAA;` at the top.
3. Run the `main()` method in **`Tests.java`** to check the algorithms.
4. Run the `main()` method in **`Experiment.java`** to measure performance.
5. After the experiment finishes, find the generated **`results.csv`** in the program's working directory. Copy it into **`results/tables/results.csv`** in the repository.
6. Keep the two graph images in **`results/plots/`** so that the images in this README appear correctly on GitHub.

The program prints the graph size, average execution times, operation counts, MST weights, and SCC counts. The CSV contains a separate row for each algorithm and graph size, making it possible to reproduce the tables and plots.

## 9. Conclusion

This assignment helped me understand that different graph problems need different algorithms. Even though all of them work with vertices and edges, their goals are not the same.

For **connecting city districts at minimum cost**, I would use Kruskal or Prim. Both find an MST, but Kruskal works by sorting edges and using DSU, while Prim grows a tree through a priority queue. For **checking whether a directed set of tasks can be completed in order**, topological sorting is useful, especially because it can also detect cycles. For **finding groups of mutually reachable locations**, Kosaraju is a good choice. For **finding the shortest routes on roads with non-negative distances**, I would use Dijkstra.

The experiments also made the complexity formulas more meaningful to me. At first, expressions such as `O(V + E)` or `O(E log E)` are just theoretical descriptions. After measuring the algorithms and counting operations, it becomes easier to see why more edges cause more work and why sorting or using a priority queue makes a difference.

The main thing I learned is that an algorithm should be chosen based on **what problem needs to be solved**, not only on which one had the lowest running time in one experiment. Correctness, graph type, weights, and data structures all matter.
