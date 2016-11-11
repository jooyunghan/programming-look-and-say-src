package ch3;

import java.util.List;

import static ch3.Lists.*;
import static java.util.Arrays.asList;

public class LookAndSay {
  public static void main(String ...args) {
    System.out.println(ant(10));
  }

  private static List<Integer> ant(int n) {
    List<Integer> s = asList(1);
    for (int i = 0; i < n; i++) {
      s = next(s);
    }
    return s;
  }

  private static List<Integer> next(List<Integer> ns) {
    return concat(map(g -> asList(g.size(), g.get(0)), group(ns)));
  }
}
