'use strict';

const {chan, go, put, CLOSED} = require('js-csp');

function next(i) {
  let o = chan()
  go(function* () {
    let prev = yield i
    let count = 1
    for (let value = yield i; value !== CLOSED; value = yield i) {
      if (value === prev) {
        count++
      } else {
        yield put(o, count)
        yield put(o, prev)
        prev = value
        count = 1
      }
    }
    yield put(o, count)
    yield put(o, prev)
    o.close()
  })
  return o
}

function init() {
  let o = chan()
  go(function* () {
    yield put(o, 1)
    o.close()
  })
  return o
}

function ant(n) {
  let ch = init()
  for (let i = 0; i < n; i++) {
    ch = next(ch)
  }
  return ch
}

const n = process.argv.length >= 3 ? parseInt(process.argv[2]) : 10
const m = process.argv.length >= 4 ? parseInt(process.argv[3]) : Infinity
let count = 0

go(function* () {
  let ch = ant(n)
  for (let v = yield ch; v !== CLOSED; v = yield ch) {
    if (count++ >= m) break
    process.stdout.write(`${v}`)
  }
  console.log()
})
