function* concat(gs) {
  for (let g of gs) {
    yield* g
  }
}

for (let a of concat([[],[1,2,3],[4],[],[5]])) {
  console.log(a)
}