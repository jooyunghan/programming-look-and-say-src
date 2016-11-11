package ch8.go;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class Command<A> {

  public <B> Command<B> then(Function<A, Command<B>> f) {
    return new Then<>(this, f);
  }

  public <B> Command<B> then(Supplier<Command<B>> f) {
    return then(a -> f.get());
  }

  public static <A> Command<A> done(A a) {
    return new Done<>(a);
  }

  public static Command<Void> print(Object... values) {
    return new Print<>(join(values), done(null));
  }

  public static Command<Void> println(Object... values) {
    return print(join(values) + "\n");
  }

  public static <A> Command<Channel<A>> chan() {
    return chan(null, 0);
  }

  public static <A> Command<Channel<A>> chan(int length) {
    return chan(null, length);
  }

  public static <A> Command<Channel<A>> chan(Class<A> cls) {
    return chan(cls, 0);
  }

  public static <A> Command<Channel<A>> chan(@SuppressWarnings("UnusedParameters") Class<A> cls, int length) {
    return new Chan<>(cls, length, ch -> done(ch));
  }

  public static <A> Command<Void> close(Channel<A> ch) {
    return new Close<>(ch, done(null));
  }

  public static <A> Command<Void> go(Command<A> forked) {
    return new Go<>(forked, done(null));
  }

  public static <A> Command<Void> send(Channel<A> ch, A value) {
    return new Send<>(ch, value, done(null));
  }

  public static <A> Command<Optional<A>> recv(Channel<A> ch) {
    return new Recv<>(ch, opt -> done(opt));
  }

  @SuppressWarnings("OptionalGetWithoutIsPresent")
  public static <A> Command<A> recv_(Channel<A> ch) {
    return new Recv<>(ch, opt -> done(opt.get()));
  }

  @SafeVarargs
  public static <A> Command<A> select(Command<A>... branches) {
    return new Select<>(asList(branches));
  }

  public static Command<Void> sleep(long ms) {
    return new Sleep<>(ms, done(null));
  }

  public static <A> Command<A> runtime(Function<Scheduler, A> f) {
    return new Runtime<>(f, a -> done(a));
  }

  static class Then<A, B> extends Command<B> {
    final Command<A> sub;
    final Function<A, Command<B>> k;

    Then(Command<A> sub, Function<A, Command<B>> k) {
      this.sub = sub;
      this.k = k;
    }

    @Override
    public <C> Command<C> then(Function<B, Command<C>> f) {
      return new Then<>(sub, a -> k.apply(a).then(f));
    }
  }

  static class Chan<C, A> extends Command<A> {
    final Class<C> cls;
    final int length;
    final Function<Channel<C>, Command<A>> next;

    Chan(Class<C> cls, int length, Function<Channel<C>, Command<A>> next) {
      this.cls = cls;
      this.length = length;
      this.next = next;
    }
  }

  static class Close<C, A> extends Command<A> {
    final Channel<C> ch;
    final Command<A> next;

    Close(Channel<C> ch, Command<A> next) {
      this.ch = ch;
      this.next = next;
    }
  }

  static class Send<C, A> extends Command<A> {
    final Channel<C> ch;
    final C value;
    final Command<A> next;

    Send(Channel<C> ch, C value, Command<A> next) {
      this.ch = ch;
      this.value = value;
      this.next = next;
    }
  }

  static class Recv<C, A> extends Command<A> {
    final Channel<C> ch;
    final Function<Optional<C>, Command<A>> next;

    Recv(Channel<C> ch, Function<Optional<C>, Command<A>> next) {
      this.ch = ch;
      this.next = next;
    }
  }

  static class Go<B, A> extends Command<A> {
    final Command<B> forked; // should be `Void` but `B` for convenience
    final Command<A> next;

    Go(Command<B> forked, Command<A> next) {
      this.forked = forked;
      this.next = next;
    }
  }

  static class Done<A> extends Command<A> {
    final A value;

    Done(A value) {
      this.value = value;
    }
  }

  static class Print<A> extends Command<A> {
    final String value;
    final Command<A> next;

    Print(String value, Command<A> next) {
      this.value = value;
      this.next = next;
    }
  }

  static class Sleep<A> extends Command<A> {
    final long ms;
    final Command<A> next;

    Sleep(long ms, Command<A> next) {
      this.ms = ms;
      this.next = next;
    }
  }

  static class Select<A> extends Command<A> {
    final List<Command<A>> branches;

    Select(List<Command<A>> branches) {
      this.branches = branches;
    }
  }

  static class Runtime<A, B> extends Command<B> {
    final Function<Scheduler, A> f;
    final Function<A, Command<B>> next;

    Runtime(Function<Scheduler, A> f, Function<A, Command<B>> next) {
      this.f = f;
      this.next = next;
    }
  }

  private static String join(Object[] values) {
    return Arrays.stream(values).map(Object::toString).collect(Collectors.joining(" "));
  }
}

