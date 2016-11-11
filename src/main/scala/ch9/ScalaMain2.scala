package ch9

object ScalaMain2 extends App {
  def ant(n: Int): Stream[Int] = {
    var s = Stream(1)
    var i = 0
    while (i < n) {
      s = next(s)
      i += 1
    }
    s
  }

  def next(s: Stream[Int]): Stream[Int] = {
    def loop(prev:Int, count:Int, s: Stream[Int]): Stream[Int] =
      if (s.isEmpty) {
        Stream(count, prev)
      } else if (s.head == prev) {
        loop(prev, count+1, s.tail)
      } else {
        count #:: prev #:: loop(s.head, 1, s.tail)
      }
    loop(s.head, 1, s.tail)
  }


  println(ant(1000000).take(100).mkString)
}
