package ch2;

import static ch2.Strings.replace;

public class LookAndSay {
  public static void main(String... args) {
    System.out.println(ant(10));
  }

  private static String ant(int n) {
    String s = "1";
    for (int i = 0; i < n; i++) {
      s = next(s);
    }
    return s;
  }

  private static String next(String s) {
    return replace(s, "(.)\\1*", m -> m.group().length() + m.group(1));
  }
}
