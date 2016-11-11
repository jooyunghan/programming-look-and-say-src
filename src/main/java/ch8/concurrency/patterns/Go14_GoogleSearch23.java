package ch8.concurrency.patterns;

import ch8.go.Command;
import ch8.go.Rand;

import static ch8.go.Control.each;
import static ch8.go.Format.q;
import static ch8.go.Time.now;
import static ch8.go.Time.since;
import static ch8.go.Command.*;
import static ch8.go.Interpreter.run;

//https://github.com/golang/talks/blob/master/2012/concurrency/support/google2.3.go
public class Go14_GoogleSearch23 {
  public static void main(String[] args) {
    run(goMain());
  }

  private static Command<Void> goMain() {
    return now().then(start ->
        First("golang", fakeSearch("replica 1"), fakeSearch("replica 2")).then(results ->
            since(start).then(elapsed ->
                println(results).then(() ->
                    println(elapsed)))));
  }

  private static Search fakeSearch(String kind) {
    return query -> sleep(Rand.intN(100)).then(() -> done(kind + " result for " + q(query) + "\n"));
  }

  private static Command<String> First(String query, Search... replicas) {
    return chan(String.class).then(c ->
        each(replicas, search ->
            go(search.apply(query).then(result ->
                send(c, result))))
            .then(() -> recv_(c)));
  }
}
