package ch9

object ScalaStreamStackOverflow extends App {
  def aStream(n: Int): Stream[Int] = {
    var s = 0 #:: Stream(1)
    var i = 0
    while (i < n) {
      i += 1
      s = wrap(s)
    }
    s
  }


  def wrap(s: Stream[Int]): Stream[Int] =
    0 #:: s.tail

  println(aStream(5000).tail.head)
}
