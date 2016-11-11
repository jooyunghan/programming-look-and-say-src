package ch9

object ScalaMain3 extends App {
  def ant: Stream[Stream[Int]] =
    Stream.iterate(Stream(1))(next)

  def next(s: Stream[Int]): Stream[Int] =
    if (s.isEmpty)
      Stream.empty
    else {
      val (g, rest) = s.span(_ == s.head)
      g.size #:: g.head #:: next(rest)
    }

  println(ant.drop(1000000).head.take(100).mkString)
}
