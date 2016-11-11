package ch8.go.clocks;

import ch8.go.Clock;

import java.time.Instant;

/**
 * Created by jooyung.han on 16. 10. 13.
 */
public class VirtualClock implements Clock {
  Instant now = Instant.ofEpochMilli(0);

  @Override
  public Instant instant() {
    return now;
  }

  @Override
  public void advanceTo(Instant instant) {
    now = instant;
  }
}
