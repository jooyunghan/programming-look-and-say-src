package ch8.go.clocks;

import ch8.go.Clock;

import java.time.Duration;
import java.time.Instant;

/**
 * Created by jooyung.han on 16. 10. 13.
 */
public class RealClock implements Clock {

  @Override
  public Instant instant() {
    return Instant.now();
  }

  @Override
  public void advanceTo(Instant instant) {
    try {
      Thread.sleep(Math.max(Duration.between(Instant.now(), instant).toMillis(), 0));
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
