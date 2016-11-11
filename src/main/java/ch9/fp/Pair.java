package ch9.fp;

public class Pair<A,B> {
  public final A _1;
  public final B _2;

  public Pair(A a, B b) {
    this._1 = a;
    this._2 = b;
  }

  public static <A,B> Pair<A,B> pair(A a, B b) {
    return new Pair<>(a, b);
  }
}
