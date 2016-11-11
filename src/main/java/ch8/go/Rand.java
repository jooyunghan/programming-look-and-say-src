package ch8.go;

import java.util.Random;

/**
 * Created by jooyung.han on 16. 10. 12.
 */
public class Rand {
  static final Random random = new Random();

  public static int intN(int max) {
    return random.nextInt(max);
  }
}
