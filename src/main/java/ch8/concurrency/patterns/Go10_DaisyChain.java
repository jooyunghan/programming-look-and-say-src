package ch8.concurrency.patterns;

import ch8.go.Channel;
import ch8.go.Command;

import static ch8.go.Control.applyN;
import static ch8.go.Command.*;
import static ch8.go.Interpreter.run;

//https://github.com/golang/talks/blob/master/2012/concurrency/support/daisy.go
public class Go10_DaisyChain {
  public static void main(String[] args) {
    run(goMain());
  }

  private static Command<Void> goMain() {
    final int n = 10000;
    return chan(int.class).then(leftmost ->
        applyN(n, left ->
            chan(int.class).then(right ->
                go(f(left, right)).then(() ->
                    done(right))), leftmost
        ).then(right ->
            go(send(right, 1))).then(() ->
            recv_(leftmost).then(v ->
                println(v))));
  }

  private static Command<Void> f(Channel<Integer> left, Channel<Integer> right) {
    return recv_(right).then(v ->
        send(left, v + 1));
  }

}
