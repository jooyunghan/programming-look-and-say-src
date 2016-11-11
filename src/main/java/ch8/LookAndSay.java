package ch8;

import ch8.go.Channel;
import ch8.go.Command;

import static ch8.go.Command.*;
import static ch8.go.Control.each;
import static ch8.go.Interpreter.run;

public class LookAndSay {

  public static void main(String[] args) {
    run(ant(10).then(ch -> each(ch, v -> print(v))));
  }

  private static Command<Channel<Integer>> ant(int n) {
    return init().then(ch -> loop0(0, n, ch));
  }

  private static Command<Channel<Integer>> init() {
    return chan(int.class)
        .then(ch -> go(send(ch, 1)
            .then(x -> close(ch)))
            .then(x -> done(ch)));
  }

  private static Command<Channel<Integer>> loop0(int i, int n,
                                                 Channel<Integer> ch) {
    if (i < n) {
      return next(ch).then(c -> loop0(i + 1, n, c));
    } else {
      return done(ch);
    }
  }

  private static Command<Channel<Integer>> next(Channel<Integer> i) {
    return chan(int.class)
        .then(o -> go(recv(i)
            .then(c -> loop1(c.get(), 1, i, o))
            .then(x -> close(o)))
            .then(x -> done(o)));
  }

  private static Command<Void> loop1(int prev, int count,
                                     Channel<Integer> i, Channel<Integer> o) {
    return recv(i)
        .then(value -> {
          if (value.isPresent()) {
            int v = value.get();
            if (v == prev)
              return loop1(prev, count + 1, i, o);
            else
              return send(o, count)
                  .then(x -> send(o, prev))
                  .then(x -> loop1(v, 1, i, o));
          } else {
            return send(o, count)
                .then(x -> send(o, prev));
          }
        });
  }
}
