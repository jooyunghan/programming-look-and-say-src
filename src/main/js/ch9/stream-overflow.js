'use strict'

function cons(head, tail) {
  return {
    head,
    tail
  }
}
function iterate(init, f) {
  return cons(init, () => iterate(f(init), f))
}

function ant(n) {
  let s = cons(1, () => cons(0, () => null))
  for (let i = 0; i < n; i++)
    s = next(s)
  return s
}

function next(s) {
  return cons(s.head, () => cons(s.tail().head + 1, () => null))
}

function trampoline(f) {
  while (typeof f === 'function')
    f = f()
  return f
}
console.log(ant(100000).tail().head)