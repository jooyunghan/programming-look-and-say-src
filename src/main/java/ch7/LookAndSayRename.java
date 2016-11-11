package ch7;

import java.util.function.Supplier;

public class LookAndSayRename {

  private static class Stream<A> {
    final A head;
    final Supplier<Stream<A>> tail;

    public Stream(A head, Supplier<Stream<A>> tail) {
      this.head = head;
      this.tail = tail;
    }
  }

  private static <A> Stream<A> cons(A head, Supplier<Stream<A>> tail) {
    return new Stream<>(head, tail);
  }

//    Stream<Stream<Integer>> ants = ants(cons(1, () -> null));
//    for (int i = 0; i < 1000000; i++)
//      ants = ants.tail.get();
//
//    Stream<Integer> line = ants.head;

  private static Stream<Stream<Integer>> ants(Stream<Integer> init) {
    return cons(init, () -> ants(next(init)));
  }

  public static void main(String[] args) {
    Stream<Integer> line = ant(1_000_000);
    while (true) {
      System.out.print(line.head);
      line = line.tail.get();
    }
  }

  private static Stream<Integer> ant(int n) {
    Stream<Integer> line = cons(1, () -> null);
    for (int i = 0; i < n; i++)
      line = next(line);
    return line;
  }

  private static Stream<Integer> next(Stream<Integer> prev) {
    if (prev == null) return null;

    final int head = prev.head;
    int count = 1;
    Stream<Integer> line = prev.tail.get();
    while (line != null && line.head == head) {
      count++;
      line = line.tail.get();
    }
    final Stream<Integer> rest = line;
    return cons(count, () -> cons(head, () -> next(rest)));
  }
}
