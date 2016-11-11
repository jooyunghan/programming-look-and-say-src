'use strict'

const READ = {type: 'read'}
const WRITE = (value) => ({type: 'write', value})

function* next() {
  let prev = yield READ
  let count = 1
  let value
  while (value = yield READ) {
    if (prev === value) {
      count++
    } else {
      yield WRITE(count)
      yield WRITE(prev)
      prev = value
      count = 1
    }
  }
  yield WRITE(count)
  yield WRITE(prev)
}

function* ant(n) {
  let procs = new Array(n + 1)
  procs[0] = function* () { yield WRITE(1) } ()
  for (let i = 1; i < n+1; i++) {
    procs[i] = next()
  }
  yield* dispatch(procs)
}

function* dispatch(procs) {
  let value
  let cur = n 
  while (true) {
    let next = procs[cur].next(value)
    if (next.done) {
      if (cur === n) {
        return
      } else {
        value = undefined
        cur++
      }
    } else {
      if (next.value.type === 'read') {
        cur--
      } else if (cur === n) {
        yield next.value.value
      } else {
        value = next.value.value
        cur++
      }
    }
  }
}

const n = process.argv.length >= 3 ? parseInt(process.argv[2]) : 10
const m = process.argv.length >= 4 ? parseInt(process.argv[3]) : 100
let count = 0

for (let v of ant(n)) {
  if (count++ >= m) break
  process.stdout.write(`${v}`)
}
console.log()