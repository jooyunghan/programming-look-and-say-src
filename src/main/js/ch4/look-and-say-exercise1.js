// next()를 한덩어리로 만들기

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

function next(it) {
  let prev = it.next().value, count = 1
  let values = []
  return {
    next() {
      while (values.length === 0) {
        let {value, done} = it.next()
        if (done) {
          if (count > 0) {
            values.push(count, prev)
            count = 0
          }
          break
        } else if (prev === value) {
          count++
        } else {
          values.push(count, prev)
          count = 1
          prev = value
        }
      }
      if (values.length > 0)
        return {done: false, value: values.shift()}
      else
        return {done: true}
    }
  }
}

for (let a of uniter(ant(100))) {
  process.stdout.write(`${a}`)
}