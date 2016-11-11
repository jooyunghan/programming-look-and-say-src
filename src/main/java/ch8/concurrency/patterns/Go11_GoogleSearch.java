package ch8.concurrency.patterns;

import ch8.go.Command;
import ch8.go.Rand;

import java.util.List;

import static ch8.go.Format.q;
import static ch8.go.Time.now;
import static ch8.go.Time.since;
import static ch8.go.Command.*;
import static ch8.go.Interpreter.run;
import static java.util.Arrays.asList;

//https://github.com/golang/talks/blob/master/2012/concurrency/support/google.go
public class Go11_GoogleSearch {
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
    return Web.apply(query).then(r1 -> Image.apply(query).then(r2 -> Video.apply(query).then(r3 -> done(asList(r1, r2, r3)))));
  }
}
