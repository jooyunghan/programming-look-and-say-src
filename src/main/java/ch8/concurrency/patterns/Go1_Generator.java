package ch8.concurrency.patterns;

import ch8.go.Channel;
import ch8.go.Command;
import ch8.go.Rand;

import static ch8.go.Command.*;
import static ch8.go.Control.loopI;
import static ch8.go.Control.loopN;
import static ch8.go.Format.q;
import static ch8.go.Interpreter.run;

public class Go1_Generator {
  public static void main(String[] args) {
    run(goMain());
  }

  private static Command<Void> goMain() {
    return boring("boring!")
        .then(c -> loopN(5, recv_(c).then(msg -> println("You say: " + q(msg)))))
        .then(() -> println("You're boring; I'm leaving."));
  }

  private static Command<Channel<String>> boring(String msg) {
    return chan(String.class)
        .then(c -> go(loopI(0, i -> send(c, msg + ": " + i)
            .then(() -> sleep(Rand.intN(1000)))))
            .then(() -> done(c)));
  }

}
