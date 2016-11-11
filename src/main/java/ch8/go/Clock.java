package ch8.go;

import java.time.Instant;

public interface Clock {
  Instant instant();
  void advanceTo(Instant instant);
}
