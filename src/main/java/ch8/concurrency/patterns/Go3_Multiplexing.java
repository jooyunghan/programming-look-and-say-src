package ch8.concurrency.patterns;

import ch8.go.Channel;
import ch8.go.Command;
import ch8.go.Rand;

import static ch8.go.Command.*;
import static ch8.go.Control.*;
import static ch8.go.Interpreter.run;

public class Go3_Multiplexing {
  public static void main(String[] args) {
    run(goMain());
  }

  private static Command<Void> goMain() {
    return boring("Joe")
        .then(joe -> boring("Ann").then(ann -> fanIn(joe, ann)))
        .then(c -> loopN(10, recv_(c).then(msg -> println(msg))))
        .then(() -> println("You're both boring; I'm leaving."));
  }

  private static <A> Command<Channel<A>> fanIn(Channel<A> input1, Channel<A> input2) {
    return Command.<A>chan()
        .then(c -> go(loop(recv_(input1).then(v -> send(c, v))))
            .then(() -> go(loop(recv_(input2).then(v -> send(c, v)))))
            .then(() -> done(c)));
  }

  private static Command<Channel<String>> boring(String msg) {
    return chan(String.class)
        .then(c -> go(loopI(0, i -> send(c, msg + ": " + i)
            .then(() -> sleep(Rand.intN(1000)))))
            .then(() -> done(c)));
  }
}
