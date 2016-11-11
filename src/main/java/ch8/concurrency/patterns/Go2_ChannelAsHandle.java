package ch8.concurrency.patterns;

import ch8.go.Channel;
import ch8.go.Command;
import ch8.go.Rand;

import static ch8.go.Command.*;
import static ch8.go.Control.loopI;
import static ch8.go.Control.loopN;
import static ch8.go.Interpreter.run;

public class Go2_ChannelAsHandle {
  public static void main(String[] args) {
    run(goMain());
  }

  private static Command<Void> goMain() {
    return boring("Joe")
        .then(joe -> boring("Ann")
            .then(ann -> loopN(5, recv_(joe).then(msg -> println(msg))
                .then(() -> recv_(ann).then(msg -> println(msg))))))
        .then(() -> println("You're both boring; I'm leaving."));
  }

  private static Command<Channel<String>> boring(String msg) {
    return chan(String.class)
        .then(c -> go(loopI(0, i -> send(c, msg + ": " + i)
            .then(() -> sleep(Rand.intN(1000)))))
            .then(() -> done(c)));
  }

}
