**Table 1. Kruskal vs Prim (connected undirected graphs, 5 runs, average)**

| Graph | V | E | Algorithm | Avg time (µs) | Edge examinations | Comparisons | DSU ops (find+union) | PQ ops (push+poll) | MST weight | Connected |
|---|---|---|---|---|---|---|---|---|---|---|
| Small | 10 | 30 | Kruskal | 2.14 | 15 | 109 | 45 | - | 134 | true |
| Small | 10 | 30 | Prim | 1.76 | 60 | 204 | - | 60 | 134 | true |
| Medium | 100 | 500 | Kruskal | 68.46 | 292 | 3844 | 876 | - | 1301 | true |
| Medium | 100 | 500 | Prim | 40.04 | 1000 | 7661 | - | 1000 | 1301 | true |
| Large | 1000 | 5000 | Kruskal | 983.42 | 3629 | 50356 | 10887 | - | 12002 | true |
| Large | 1000 | 5000 | Prim | 912.66 | 10000 | 110757 | - | 10000 | 12002 | true |

**Table 2. DSU stress test: n = 20000, union(i,i+1) for all i, then find(i) for all i**

| DSU variant | Avg time (ms) | Parent-pointer hops in find | find calls | union calls |
|---|---|---|---|---|
| naive (no optimizations) | 335.18 | 199990000 | 59998 | 19999 |
| path compression only | 0.21 | 39997 | 59998 | 19999 |
| union by rank only | 0.16 | 39997 | 59998 | 19999 |
| path compression + union by rank | 0.19 | 39997 | 59998 | 19999 |

**Table 3. Kruskal on the Large graph (V=1000, E=5000) with different DSU variants**

| DSU variant | Avg time (µs) | Parent-pointer hops in find | MST weight |
|---|---|---|---|
| naive (no optimizations) | 2634.90 | 608970 | 12002 |
| path compression only | 1124.50 | 13420 | 12002 |
| union by rank only | 1007.04 | 10979 | 12002 |
| path compression + union by rank | 903.66 | 6709 | 12002 |

**Table 4. Kosaraju SCC on random directed graphs (cycles allowed)**

| Graph | V | E | Avg time (µs) | Edge examinations | # SCC | Largest SCC | Contains cycle |
|---|---|---|---|---|---|---|---|
| Small | 10 | 30 | 1.34 | 60 | 1 | 10 | true |
| Medium | 100 | 500 | 79.36 | 1000 | 6 | 95 | true |
| Large | 1000 | 5000 | 683.12 | 10000 | 15 | 986 | true |

**Table 5. Topological ordering (Kahn): DAGs vs graphs with cycles**

| Graph | V | E | Input | Avg time (µs) | Edge examinations | Is DAG | Vertices ordered | Order valid |
|---|---|---|---|---|---|---|---|---|
| Small | 10 | 30 | DAG | 0.42 | 60 | true | 10 | true |
| Small | 10 | 30 | with cycles | 0.22 | 30 | false | 0 | n/a (cycle detected) |
| Medium | 100 | 500 | DAG | 4.48 | 1000 | true | 100 | true |
| Medium | 100 | 500 | with cycles | 1.20 | 510 | false | 3 | n/a (cycle detected) |
| Large | 1000 | 5000 | DAG | 81.44 | 10000 | true | 1000 | true |
| Large | 1000 | 5000 | with cycles | 15.44 | 5042 | false | 9 | n/a (cycle detected) |

**Table 6. Dijkstra from source 0 on random directed graphs (weights 1..100)**

| Graph | V | E | Avg time (µs) | Edge relaxations (examined) | Successful relaxations | PQ pushes | PQ polls | Comparisons | Reachable vertices |
|---|---|---|---|---|---|---|---|---|---|
| Small | 10 | 30 | 0.52 | 30 | 12 | 13 | 13 | 33 | 10/10 |
| Medium | 100 | 500 | 10.82 | 490 | 146 | 147 | 147 | 1578 | 97/100 |
| Large | 1000 | 5000 | 351.20 | 4958 | 1442 | 1443 | 1443 | 24191 | 991/1000 |

