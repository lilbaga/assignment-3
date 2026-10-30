**Table 6. Dijkstra from source 0 on random directed graphs (weights 1..100)**

| Graph | V | E | Avg time (µs) | Edge relaxations (examined) | Successful relaxations | PQ pushes | PQ polls | Comparisons | Reachable vertices |
|---|---|---|---|---|---|---|---|---|---|
| Small | 10 | 30 | 0.52 | 30 | 12 | 13 | 13 | 33 | 10/10 |
| Medium | 100 | 500 | 10.82 | 490 | 146 | 147 | 147 | 1578 | 97/100 |
| Large | 1000 | 5000 | 351.20 | 4958 | 1442 | 1443 | 1443 | 24191 | 991/1000 |
