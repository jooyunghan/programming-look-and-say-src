package ch8.concurrency.patterns;

import ch8.go.Channel;
import ch8.go.Command;
import ch8.go.Rand;

import static ch8.go.Command.*;
import static ch8.go.Control.loopI;
import static ch8.go.Control.loopN;
import static ch8.go.Interpreter.run;

//https://github.com/golang/talks/blob/master/2012/concurrency/support/quit.go
public class Go8_QuitChannel {
  public static void main(String[] args) {
    run(goMain());
  }

  private static Command<Void> goMain() {
    return chan(Boolean.class).then(quit ->
        boring("Joe", quit).then(c ->
            loopN(Rand.intN(10),
                recv_(c).then(s ->
                    println(s))
            ).then(() -> send(quit, true))));
  }

  private static Command<Channel<String>> boring(String msg, Channel<Boolean> quit) {
    return chan(String.class).then(c ->
        go(loopI(0, i ->
            sleep(Rand.intN(1000)).then(() ->
                select(
                    send(c, msg + ": " + i).then(() -> done(true)),
                    recv(quit).then(() -> done(false))
                )))
        ).then(() -> done(c)));
  }
}
