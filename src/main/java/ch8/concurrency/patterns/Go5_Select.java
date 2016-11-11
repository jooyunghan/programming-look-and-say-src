package ch8.concurrency.patterns;

import ch8.go.Channel;
import ch8.go.Command;
import ch8.go.Rand;

import static ch8.go.Command.*;
import static ch8.go.Control.*;
import static ch8.go.Interpreter.run;

public class Go5_Select {
  public static void main(String[] args) {
    run(goMain());
  }

  static class Message {
    final String str;
    final Channel<Boolean> wait;

    Message(String str, Channel<Boolean> wait) {
      this.str = str;
      this.wait = wait;
    }
  }

  private static Command<Void> goMain() {
    return boring("Joe")
        .then(joe -> boring("Ann").then(ann -> fanIn(joe, ann)))
        .then(c -> loopN(5, recv_(c).then(msg1 -> println(msg1.str)
            .then(() -> recv_(c).then(msg2 -> println(msg2.str)
                .then(() -> send(msg1.wait, true))
                .then(() -> send(msg2.wait, true)))))))
        .then(() -> println("You're all boring; I'm leaving."));
  }

  private static <A> Command<Channel<A>> fanIn(Channel<A> input1, Channel<A> input2) {
    return Command.<A>chan()
        .then(c -> go(loop(select(
            recv(input1).then(v -> send(c, v.get())),
            recv(input2).then(v -> send(c, v.get()))
        ))).then(() -> done(c)));
  }

  private static Command<Channel<Message>> boring(String msg) {
    return chan(Message.class)
        .then(c -> chan(boolean.class)
            .then(wait -> go(
                loopI(0, i -> send(c, new Message(msg + ": " + i, wait))
                    .then(() -> sleep(Rand.intN(2000)))
                    .then(() -> recv(wait)))))
            .then(() -> done(c)));
  }
}
