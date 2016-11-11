package ch8.concurrency.patterns;

import ch8.go.Channel;
import ch8.go.Command;
import ch8.go.Rand;
import ch8.go.Time;

import static ch8.go.Control.loopI;
import static ch8.go.Control.loopWhile;
import static ch8.go.Command.*;
import static ch8.go.Interpreter.run;

//https://github.com/golang/talks/blob/master/2012/concurrency/support/timeout.go
public class Go6_TimeOutUsingSelect {
  public static void main(String[] args) {
    run(goMain());
  }

  private static Command<Void> goMain() {
    return boring("Joe")
        .then(c -> loopWhile(Time.after(1000)
            .then(timer ->
                select(
                    recv_(c).then(s -> println(s)).then(() -> done(true)),
                    recv(timer).then(() -> println("You're all boring; I'm leaving.").then(() -> done(false)))
                ))));
  }

  private static Command<Channel<String>> boring(String msg) {
    return chan(String.class)
        .then(c -> go(loopI(0, i -> send(c, msg + ": " + i).then(() -> sleep(Rand.intN(1500)))))
            .then(() -> done(c)));
  }

}
