package ch8.concurrency.patterns;

import ch8.go.Command;
import ch8.go.Rand;

import java.util.ArrayList;
import java.util.List;

import static ch8.go.Command.*;
import static ch8.go.Control.loopN;
import static ch8.go.Format.q;
import static ch8.go.Interpreter.run;
import static ch8.go.Time.now;
import static ch8.go.Time.since;

//https://github.com/golang/talks/blob/master/2012/concurrency/support/google2.1.go
public class Go12_GoogleSearch20 {
  public static void main(String[] args) {
    run(goMain());
  }

  private static Command<Void> goMain() {
    return now().then(start ->
        Google("golang").then(results -> since(start).then(elapsed -> println(results).then(() -> println(elapsed)))));
  }

  private static final Search Web = fakeSearch("web");
  private static final Search Image = fakeSearch("image");
  private static final Search Video = fakeSearch("video");

  private static Search fakeSearch(String kind) {
    return query -> sleep(Rand.intN(100)).then(() -> done(kind + " result for " + q(query) + "\n"));
  }

  private static Command<List<String>> Google(String query) {
    List<String> results = new ArrayList<>();
    return chan(String.class).then(c ->
        go(Web.apply(query).then(r -> send(c, r)))
            .then(() -> go(Image.apply(query).then(r -> send(c, r))))
            .then(() -> go(Video.apply(query).then(r -> send(c, r))))
            .then(() -> loopN(3, recv_(c).then(r -> act(() -> results.add(r))))))
        .then(() -> done(results));
  }

  private static Command<Void> act(Runnable f) {
    f.run();
    return done(null);
  }
}
