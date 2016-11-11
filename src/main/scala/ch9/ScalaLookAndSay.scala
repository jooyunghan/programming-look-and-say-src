package ch9

object ScalaLookAndSay extends App {
  def ant: Stream[Stream[Int]] =
    Stream.iterate(Stream(1))(next)

  def next(s: Stream[Int]): Stream[Int] =
    group(s).flatMap(g => Stream(g.size, g.head))

  def group[A](s: Stream[A]): Stream[Seq[A]] =
    s match {
      case h #:: _ => val (g, rest) = s.span(_ == h); g #:: group(rest)
      case _ => Stream.empty
    }

  println(ant.drop(1000000).head.take(100).mkString)
}
