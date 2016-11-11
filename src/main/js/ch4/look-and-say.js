function group(it) {
  let g = null
  return {
    next() {
      while (true) {
        let {value, done} = it.next()
        if (done && g === null) {
          return { done: true }
        } else if (done) {
          let result = g
          g = null
          return { done: false, value: result }
        } else if (g === null) {
          g = [value]
        } else if (g[0] === value) {
          g.push(value)
        } else {
          let result = g
          g = [value]
          return { done: false, value: result }
        }
      }
    }
  }
}

function concat(it) {
  let inner = null
  return {
    next() {
      while (true) {
        if (inner === null) {    // 다음 내부 이터레이터 찾기
          let {value, done} = it.next()
          if (done) {
            return { done: true } // 외부 이터레이터가 끝나면 종료
          } else {
            inner = value
          }
        }
        let {value, done} = inner.next()
        if (done) {
          inner = null
        } else {
          return { done: false, value } // 내부 이터레이터의 다음 값 반환
        }
      }
    }
  }
}

function map(f, it) {
  return {
    next() {
      let {value, done} = it.next()
      if (done) {
        return { done: true }
      } else {
        return { done: false, value: f(value) }
      }
    }
  }
}

function iter(obj) {
  return obj[Symbol.iterator]()
}

function uniter(it) {
  return {
    [Symbol.iterator]: function () {
      return it
    }
  }
}

function ant(n) {
  let s = iter([1])
  for (let i = 0; i < n; i++) {
    s = next(s)
  }
  return s
}

function next(ns) {
  return concat(map(g => iter([g.length, g[0]]), group(ns)))
}

for (let a of uniter(ant(100))) {
  process.stdout.write(`${a}`)
}