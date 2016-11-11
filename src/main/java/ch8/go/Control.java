package ch8.go;

import java.util.function.Function;

import static ch8.go.Command.done;
import static ch8.go.Command.recv;

public class Control {

  public static <A, B> Command<B> each(A[] as, Function<A, Command<B>> body) {
    return loopIN(0, as.length, i -> body.apply(as[i]));
  }

  public static <A, B> Command<Void> each(Channel<A> ch, Function<A, Command<B>> f) {
    return loopWhile(recv(ch).then(o -> o.isPresent() ? f.apply(o.get()).then(() -> done(true)) : done(false)));
  }

  public static <A> Command<A> loop(Command<A> body) {
    return body.then(() -> loop(body));
  }

  public static <A> Command<A> loopI(int i, Function<Integer, Command<A>> body) {
    return body.apply(i).then(() -> loopI(i + 1, body));
  }

  public static <A> Command<A> loopIN(int i, int n, Function<Integer, Command<A>> body) {
    return i < n ? body.apply(i).then(() -> loopIN(i + 1, n, body)) : done(null);
  }

  public static <A> Command<A> loopN(int n, Command<A> body) {
    return loopIN(0, n, i -> body);
  }

  public static Command<Void> loopINWhile(int i, int n, Function<Integer, Command<Boolean>> body) {
    return i < n ? body.apply(i).then(cont -> cont ? loopINWhile(i + 1, n, body) : done(null)) : done(null);
  }

  public static Command<Void> loopWhile(Command<Boolean> body) {
    return body.then(cont -> cont ? loopWhile(body) : done(null));
  }

  public static <A> Command<A> applyN(int n, Function<A, Command<A>> f, A initial) {
    if (n == 0)
      return done(initial);
    else
      return f.apply(initial).then(next -> applyN(n - 1, f, next));
  }
}
