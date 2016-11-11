package ch8;

import ch8.go.Channel;
import ch8.go.Command;

import static ch8.go.Control.loopI;
import static ch8.go.Command.*;
import static ch8.go.Interpreter.run;

public class PingPong {
  public static void main(String[] args) {
    run(pingpong());
  }

  private static Command<Void> pingpong() {
    return chan(Ball.class)
        .then(table -> go(player("ping", table))
            .then(() -> go(player("pong", table)))
            .then(() -> send(table, new Ball()))
            .then(() -> sleep(1000))
            .then(() -> recv(table)))
        .then(() -> done(null));
  }

  private static Command<Void> player(String name, Channel<Ball> table) {
    return loopI(1, i -> recv_(table)
        .then(ball -> {
          ball.hits++;
          return println(name, i, "hit", ball.hits)
              .then(() -> sleep(100))
              .then(() -> send(table, ball));
        }));
  }

  static class Ball {
    public int hits = 0;
  }
}
