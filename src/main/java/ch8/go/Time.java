package ch8.go;

import java.time.Duration;
import java.time.Instant;

import static ch8.go.Command.*;

public class Time {
  public static Command<Instant> now() {
    return runtime(scheduler -> scheduler.now());
  }

  public static Command<Duration> since(Instant from) {
    return now().then(now -> done(Duration.between(from, now)));
  }

  public static Command<Channel<Boolean>> after(int ms) {
    return chan(Boolean.class)
        .then(c -> go(sleep(ms).then(() -> send(c, true)))
            .then(() -> done(c)));
  }
}
