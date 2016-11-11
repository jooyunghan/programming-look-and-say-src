package ch7;

import ch7.cont.Cont;

import static ch7.cont.Cont.*;

public class LookAndSay {
  public static void main(String[] args) {
    run(ant(1_000_000), n -> System.out.print(n));
  }

  private static Cont<Integer> ant(int n) {
    if (n == 0) return write(1, () -> done()); // 첫 줄
    return pipe(() -> ant(n - 1), () -> next());
  }

  private static Cont<Integer> next() {
    return read(value -> loop(value.get(), 1));
  }

  private static Cont<Integer> loop(int prev, int count) {
    return read(value -> {
      if (!value.isPresent()) {
        return write(count, () -> write(prev, () -> done()));
      } else if (value.get() == prev) {
        return loop(prev, count + 1);
      } else {
        return write(count, () -> write(prev, () -> loop(value.get(), 1)));
      }
    });
  }
}
