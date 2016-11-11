import io.herrmann.generator.Generator;

import java.util.Iterator;

public class LookAndSay {

    public static void main(String[] args) {
        Generator<Integer> s = ant(1000);

        Iterator<Integer> it = s.iterator();
        int count = 0;
        while (it.hasNext()) {
            System.out.print(it.next());
            if (count++ % 30 == 0) {
                System.out.println(" " + count);
            }
        }
    }

    private static Generator<Integer> ant(int n) {
        Generator<Integer> s = gen(1);
        for (int i = 0; i < n; i++) {
            s = next(s);
        }
        return s;
    }

    @SafeVarargs
    private static <T> Generator<T> gen(T... values) {
        return new Generator<T>() {
            @Override
            protected void run() throws InterruptedException {
                for (T value : values) {
                    yield(value);
                }
            }
        };
    }

    private static
Generator<Integer> next(Generator<Integer> inner) {
  return new Generator<Integer>() {
    @Override
    protected void run() throws InterruptedException {
      Iterator<Integer> it = inner.iterator();
      int prev = it.next();
      int count = 1;
      while (it.hasNext()) {
        int c = it.next();
        if (prev == c)
          count++;
        else {
          yield(count);
          yield(prev);
          prev = c;
          count = 1;
        }
      }
      yield(count);
      yield(prev);
    }
  };
}
}