
'use strict'

function ant(n) {
  let s = cons(1, null)
  for (let i = 0; i < n; i++)
    s = next(s)
  return s
}

function next(s) {
  let head, tail
  while (s) {
    let hd = s.head
    let count = 1
    for (s = s.tail; s && hd === s.head; s = s.tail) {
      count++
    }
    if (!head) {
      head = cons(count, cons(hd, null))
      tail = head.tail
    } else {
      tail.tail = cons(count, cons(hd, null))
      tail = tail.tail.tail
    }
  }
  return head
}

function cons(head, tail) {
  return { head, tail }
}

let line = ant(10)
for (let i = 0; i < 100; i++) {
  if (!line) break
  process.stdout.write(`${line.head}`)
  line = line.tail
}
console.log()