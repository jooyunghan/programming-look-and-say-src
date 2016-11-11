package ch9;

import ch9.lazy.Stream;

import static ch9.lazy.Stream.cons;
import static ch9.lazy.Stream.stream;

public class StreamStackOverflow {
  public static void main(String[] args) {
    System.out.println(noStackOverflow());
    System.out.println(stackOverflow());
  }

  private static Stream<Integer> stackOverflow() {
    Stream<Integer> s = stream(0, 0);
    for (int i = 0; i < 1_000_000; i++) {
      final Stream<Integer> s2 = s;
      s = cons(() -> 0, () -> s2.tail());
    }
    return s.tail(); // 스택오버플로 발생
  }

  private static Integer noStackOverflow() {
    Stream<Integer> s = stream(0, 0);
    for (int i = 0; i < 1_000_000; i++) {
      final Stream<Integer> tail = s.tail();
      s = cons(() -> 0, () -> stream(tail.head() + 1));
    }
    return s.tail().head();
  }

}
