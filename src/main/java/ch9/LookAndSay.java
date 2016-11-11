package ch9;

import ch9.lazy.Stream;

import static ch9.lazy.Stream.*;

public class LookAndSay {
  public static void main(String[] args) {
    System.out.println(ant().drop(1_000_000).head().take(1000).toList());
  }

  private static Stream<Stream<Integer>> ant() {
    return iterate(s -> next(s), stream(1));
  }

  private static Stream<Integer> next(Stream<Integer> s) {
    return group(s).flatMap(g -> stream(g.size(), g.head()));
  }
}
