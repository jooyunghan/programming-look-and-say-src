package ch3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

class Lists {
  public static <A> List<List<A>> group(List<A> as) {
    List<List<A>> ass = new ArrayList<>();
    List<A> g = null;
    for (A a : as) {
      if (g == null || !g.get(0).equals(a)) {
        g = new ArrayList<>();
        ass.add(g);
      }
      g.add(a);
    }
    return ass;
  }

  public static <A, B> List<B> map(Function<A, B> f, List<A> as) {
    List<B> bs = new ArrayList<>();
    for (A a : as) {
      bs.add(f.apply(a));
    }
    return bs;
  }

  public static <A> List<A> concat(List<List<A>> ass) {
    List<A> list = new ArrayList<>();
    for (List<A> as : ass) {
      list.addAll(as);
    }
    return list;
  }
}