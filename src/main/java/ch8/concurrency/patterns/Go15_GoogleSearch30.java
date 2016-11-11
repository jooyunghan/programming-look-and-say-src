package ch8.concurrency.patterns;

import ch8.go.Command;
import ch8.go.Rand;

import java.util.ArrayList;
import java.util.List;

import static ch8.go.Command.*;
import static ch8.go.Control.each;
import static ch8.go.Control.loopINWhile;
import static ch8.go.Format.q;
import static ch8.go.Interpreter.run;
import static ch8.go.Time.*;

//https://github.com/golang/talks/blob/master/2012/concurrency/support/google3.0.go
public class Go15_GoogleSearch30 {
  public static void main(String[] args) {
    run(goMain());
  }

  private static Command<Void> goMain() {
    return now().then(start ->
        Google("golang").then(results -> since(start).then(elapsed -> println(results).then(() -> println(elapsed)))));
  }

  private static final Search Web1 = fakeSearch("web 1");
  private static final Search Web2 = fakeSearch("web 2");
  private static final Search Image1 = fakeSearch("image 1");
  private static final Search Image2 = fakeSearch("image 2");
  private static final Search Video1 = fakeSearch("video 1");
  private static final Search Video2 = fakeSearch("video 2");

  private static Search fakeSearch(String kind) {
    return query -> sleep(Rand.intN(100)).then(() -> done(kind + " result for " + q(query) + "\n"));
  }

  private static Command<List<String>> Google(String query) {
    List<String> results = new ArrayList<>();
    return
        chan(String.class)
            .then(c -> go(First(query, Web1, Web2).then(r -> send(c, r)))
                .then(() -> go(First(query, Image1, Image2).then(r -> send(c, r))))
                .then(() -> go(First(query, Video1, Video2).then(r -> send(c, r))))
                .then(() -> after(80))
                .then(timeout -> loopINWhile(0, 3, i -> select(
                    recv_(c)
                        .then(result -> act(() -> results.add(result))) // TODO then overload 추가
                        .then(() -> done(true)),
                    recv(timeout)
                        .then(() -> println("timed out"))
                        .then(() -> done(false)))))
                .then(() -> done(results)));
  }

  private static Command<String> First(String query, Search... replicas) {
    return chan(String.class).then(c ->
        each(replicas, search ->
            go(search.apply(query).then(result ->
                send(c, result))))
            .then(() -> recv_(c)));
  }

  private static Command<Void> act(Runnable f) {
    f.run();
    return done(null);
  }

}
