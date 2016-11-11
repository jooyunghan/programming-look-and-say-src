package ch7;

import java.util.Iterator;
import java.util.function.Supplier;

public class LookAndSayStreamIterator {

  private static class Stream<A> implements Iterable<A> {
    final A head;
    final Supplier<Stream<A>> tail;

    public Stream(A head, Supplier<Stream<A>> tail) {
      this.head = head;
      this.tail = tail;
    }

    @Override
    public Iterator<A> iterator() {
      return new Iterator<A>() {
        Stream<A> cur = Stream.this;

        @Override
        public boolean hasNext() {
          return cur != null;
        }

        @Override
        public A next() {
          A result = cur.head;
          cur = cur.tail.get();
          return result;
        }
      };
    }
  }

  private static <A> Stream<A> cons(A head, Supplier<Stream<A>> tail) {
    return new Stream<>(head, tail);
  }

  public static void main(String[] args) {
    for (int a : ant(1_000_000))
      System.out.print(a);
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
