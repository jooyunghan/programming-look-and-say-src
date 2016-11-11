package ch7;

import java.util.function.Supplier;

public class LookAndSayOnlyWrite {

  private static class Write {
    final int value;
    final Supplier<Write> next;

    public Write(int value, Supplier<Write> next) {
      this.value = value;
      this.next = next;
    }
  }

  private static Write write(int value, Supplier<Write> next) {
    return new Write(value, next);
  }

  public static void main(String[] args) {
    hello(world());
    Write line = ant(1_000_000);
    for (int i = 0; i < 100; i++) {
      System.out.print(line.value);
      line = line.next.get();
    }
    System.out.println();
  }

  private static void hello(Write s) {
    System.out.print("Hello");
    s.next.get();
    System.out.println(" !!!");
  }

  private static Write world() {
    return write(0, () -> {
      System.out.print(" World");
      return null;
    });
  }

  private static Write ant(int n) {
    Write line = write(1, () -> null); // 첫 줄
    for (int i = 0; i < n; i++)
      line = next(line);
    return line;
  }

  private static Write next(Write prev) {
    if (prev == null) return null;

    final int value = prev.value;
    int count = 1;
    Write line = prev.next.get(); // 사실 제너레이터와 거의 같은데 왜 stack overflow 안될까? 이 경우는 생성시 write까지 진행된 상태
    while (line != null && line.value == value) {
      count++;
      line = line.next.get();
    }
    final Write line2 = line;
    return write(count, () -> write(value, () -> next(line2)));
  }
}
