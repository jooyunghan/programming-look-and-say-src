package ch9.lazy;

import ch9.fp.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static ch9.fp.Pair.pair;

public abstract class Stream<A> {
  public static <A> Stream<A> stream(A... as) {
    return stream_(0, as);
  }

  private static <A> Stream<A> stream_(int i, A... as) {
    return i < as.length ? cons(() -> as[i], () -> stream_(i + 1, as)) : empty();
  }

  public static <A> Stream<A> cons(Supplier<A> head, Supplier<Stream<A>> tail) {
    return new Cons<>(head, tail);
  }

  public static <A> Stream<A> empty() {
    return (Stream<A>) NIL;
  }

  private static Nil<Object> NIL = new Nil<>();

  public static <A> Stream<Stream<A>> group(Stream<A> as) {
    if (as.isEmpty()) return empty();
    Pair<Stream<A>, Stream<A>> span = as.span(a -> a.equals(as.head()));
    return cons(() -> span._1, () -> group(span._2));
//    if (as.isEmpty()) return empty();
//    Predicate<A> p = x -> x.equals(as.head());
//    return cons(() -> as.takeWhile(p), () -> group(as.dropWhile(p)));
  }

  public Pair<Stream<A>, Stream<A>> span(Predicate<A> p) {
    if (this.isEmpty()) return pair(empty(), empty());
    if (!p.test(head())) return pair(empty(), this);

    Pair<Stream<A>, Stream<A>> result = tail().span(p);
    return pair(cons(() -> head(), () -> result._1), result._2);
  }

  public static <A> Stream<A> iterate(Function<A, A> f, A a) {
    return cons(() -> a, () -> iterate(f, f.apply(a)));
  }

  abstract public boolean isEmpty();
  abstract public A head();
  abstract public Stream<A> tail();
  abstract public <B> B fold(Function<Cons<A>, B> cons, Supplier<B> nil);

  public <B> Stream<B> map(Function<A, B> f) {
    return this.<Stream<B>>fold(
        cons -> cons(() -> f.apply(cons.head()), () -> cons.tail().map(f)),
        () -> empty()
    );
  }

  public <B> Stream<B> flatMap(Function<A, Stream<B>> f) {
    return join(map(f));
  }

  public <B> Stream<B> join() {
    return join((Stream) this);
  }

  public static <A> Stream<A> join(Stream<Stream<A>> ass) {
    return ass.<Stream<A>>fold(
        cons -> cons.head().append(() -> join(cons.tail())),
        () -> empty()
    );
  }

  public Stream<A> append(Supplier<Stream<A>> other) {
    return this.<Stream<A>>fold(
        cons -> cons(() -> cons.head(), () -> cons.tail().append(other)),
        () -> other.get()
    );
  }

  public Stream<A> take(int n) {
    if (n == 0) return empty();
    return this.<Stream<A>>fold(
        cons -> cons(() -> cons.head(), () -> cons.tail().take(n - 1))
        , () -> empty());
  }

  public Stream<A> takeWhile(Predicate<A> p) {
    return this.<Stream<A>>fold(
        cons -> p.test(cons.head()) ? cons(() -> cons.head(), () -> cons.tail().takeWhile(p)) : empty(),
        () -> empty()
    );
  }

  public int size() {
    int size = 0;
    Stream<A> c = this;
    while (!c.isEmpty()) {
      size++;
      c = c.tail();
    }
    return size;
  }

  public void each(Consumer<A> consumer) {
    Stream<A> c = this;
    while (!c.isEmpty()) {
      consumer.accept(c.head());
      c = c.tail();
    }
  }

  public List<A> toList() {
    List<A> result = new ArrayList<>();
    for (Stream<A> c = this; !c.isEmpty(); c = c.tail()) {
      result.add(c.head());
    }
    return result;
  }

  public Stream<A> dropWhile(Predicate<A> p) {
    Stream<A> c = this;
    while (!c.isEmpty() && p.test(c.head())) {
      c = c.tail();
    }
    return c;
  }

  public Stream<A> drop(int n) {
    Stream<A> c = this;
    while (!c.isEmpty() && n > 0) {
      n--;
      c = c.tail();
    }
    return c;
  }

//  @Override
//  public String toString() {
//    final Stream<A> tenth = drop(10);
//    return tenth.isEmpty()
//        ? toList().toString()
//        : take(10).map(a -> a.toString()).append(() -> stream("?")).toList().toString();
//  }

  private static class Lazy<A> {
    private Supplier<A> comp;
    private A value;

    public Lazy(Supplier<A> comp) {
      this.comp = comp;
    }

    public A value() {
      if (comp != null) {
        value = comp.get();
        comp = null;
      }
      return value;
    }
  }

  private static class Cons<A> extends Stream<A> {
    private Lazy<A> head;
    private Lazy<Stream<A>> tail;

    public Cons(Supplier<A> head, Supplier<Stream<A>> tail) {
      this.head = new Lazy<>(head);
      this.tail = new Lazy<>(tail);
    }

    @Override
    public boolean isEmpty() {
      return false;
    }

    @Override
    public A head() {
      return head.value();
    }

    @Override
    public Stream<A> tail() {
      return tail.value();
    }

    @Override
    public <B> B fold(Function<Cons<A>, B> cons, Supplier<B> nil) {
      return cons.apply(this);
    }
  }

  private static class Nil<A> extends Stream<A> {

    @Override
    public boolean isEmpty() {
      return true;
    }

    @Override
    public A head() {
      throw new NoSuchElementException();
    }

    @Override
    public Stream<A> tail() {
      throw new NoSuchElementException();
    }

    @Override
    public <B> B fold(Function<Cons<A>, B> cons, Supplier<B> nil) {
      return nil.get();
    }
  }
}
