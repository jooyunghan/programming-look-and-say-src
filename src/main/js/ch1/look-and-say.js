function ant(n) {
  let s = "1"
  for (let i = 0; i < n; i++) {
    s = next(s)
  }
  return s
}

function next(ns) {
  let result = ""
  for (let i = 0; i < ns.length; i++) {
    let length = 1
    while (i+1 < ns.length && ns[i] === ns[i+1]) {
      length++
      i++
    }
    result += length + ns[i]
  }
  return result
}

console.log(ant(10))