'use strict'

function cons(head, tail) {
  return { head, tail }
}

function ant(init = cons(1, () => null)) {
  return cons(init, () => ant(next(init)))
}

function next(s) {
  if (!s) return null
  let head = s.head
  let count = 1
  for (s = s.tail(); s && head === s.head; s = s.tail()) {
    count++
  }
  return cons(count, () => cons(head, () => next(s)))
}

let lines = ant()
for (let i = 0; i < 1000000; i++) {
  lines = lines.tail()
}

let line = lines.head
for (let i = 0; i < 100; i++) {
  process.stdout.write(`${line.head}`)
  line = line.tail()
}

