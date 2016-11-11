"use strict"

function read(next) {
  return {op: 'read', next}
}

function write(value, next) {
  return {op: 'write', value, next}
}

// process stack의 top을 우선 실행
// read 할 때 readers stack에 넣고
// 다음 process 실행.
// write할 때 readers stack에서 꺼내어 resume
function* dispatch(processes) {
  let readers = []
  while (true) {
    let current = processes.pop()
    if (typeof current === 'function')
      current = current()

    if (typeof current === 'undefined') { // 지금 프로세스가 종료되었다.
      if (readers.length > 0)             // 아직 읽으려는 process가 있다.
        processes.push(readers.pop())
      else                                // 마지막 프로세스가 종료되면 끝이다.
        break
    } else if (current.op === 'read') {   // 읽으려면 읽기 스택에 넣는다.
      readers.push(current.next)
    } else {                              // 쓰려면 읽기 스택에서 꺼내 재개한다.
      processes.push(current.next)
      if (readers.length > 0) {
        let next = readers.pop()
        processes.push(next(current.value))
      } else {
        yield current.value
      }
    }
  }
}

// 1 출력하고 끝
function init() {
  return write(1)
}

// 먼저 한글자 읽고 loop
function next() {
  return read(c => loop(c, 1))

  function loop(prev, count) {
    return read(c => {
      if (typeof c === 'undefined')
        return write(count, write(prev))
      else if (prev === c)
        return loop(prev, count + 1)
      else
        return write(count, write(prev, loop(c, 1)))
    })
  }
}

function* ant(n) {
  let processes = [init]
  for (let i = 0; i < n; i++)
    processes.push(next)
  yield* dispatch(processes)
}

const n = process.argv.length >= 3 ? parseInt(process.argv[2]) : 10
const m = process.argv.length >= 4 ? parseInt(process.argv[3]) : 100
let count = 0

for (let i of ant(n)) {
  if (count >= m) break
  process.stdout.write(`${i}`)
  count++
}
console.log()
