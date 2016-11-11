function ant(n) {
  let s = gen([1])
  for (let i = 0; i < n; i++)
    s = next(s)
  return s
}

function* next(g) {
  let prev = g.next().value
  let count = 1
  for (let value of g) {
    if (value === prev)
      count++
    else {
      yield* [count, prev]
      prev = value
      count = 1
    }
  }
  yield* [count, prev]
}

for (let c of ant(10))
  process.stdout.write(`${c}`)
process.stdout.write('\n')

function* gen(obj) {
  yield* obj
}