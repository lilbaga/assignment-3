**Table 2. DSU stress test: n = 20000, union(i,i+1) for all i, then find(i) for all i**

| DSU variant | Avg time (ms) | Parent-pointer hops in find | find calls | union calls |
|---|---|---|---|---|
| naive (no optimizations) | 335.18 | 199990000 | 59998 | 19999 |
| path compression only | 0.21 | 39997 | 59998 | 19999 |
| union by rank only | 0.16 | 39997 | 59998 | 19999 |
| path compression + union by rank | 0.19 | 39997 | 59998 | 19999 |
