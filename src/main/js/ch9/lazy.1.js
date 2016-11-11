'use strict'

function cons(head, tail) {
  return { head, tail }
}

function ant() {
  return iterate(cons(1, () => null), next)
}

function iterate(init, f) {
  return cons(init, () => iterate(f(init), f))
}

function next(s) {
  if (!s) return null
  let hd = s.head, count = 1
  for (s = s.tail(); s && hd === s.head; s = s.tail()) {
    count++
  }
  return cons(count, () => cons(hd, () => next(s)))
}

function drop(n, s) {
  for (let i = 0; i < n; i++) {
    s = s.tail()
  }
  return s
}

let line = drop(1000000, ant()).head

for (let i = 0; i < 100; i++) {
  process.stdout.write(`${line.head}`)
  line = line.tail()
}
console.log()