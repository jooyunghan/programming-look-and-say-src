
function ant(n) {
  let coros = []
  let index = 0;
  coros.push(init(index++))
  for (let i = 0; i < n; i++)
    coros.push(next(index++))
  coros.push(print(index++))
  run(coros)
}

function* init(n) {
  yield [n+1, 1]
  yield [n+1, undefined]
}

function* next(n) {
  let prev = yield [n-1]
  let count = 1
  while (true) {
    let value = yield [n-1]
    if (!value) break
    if (value === prev)
      count++
    else {
      yield* [[n+1,count], [n+1,prev]]
      prev = value
      count = 1
    }
  }
  yield* [[n+1,count], [n+1,prev], [n+1, undefined]]
}

function* print(n) {
  while (true) {
    let v = yield [n-1]
    if (!v) break
    process.stdout.write(`${v}`)
  }
}

function run(coros) {
  let n = coros.length - 1
  let v
  while (true) {
    let {done, value} = coros[n].next(v)
    if (done) break
    n = value[0]
    v = value[1]
  }
}

ant(10)