package ch8.go;

import com.google.gson.Gson;

public class Format {

  static final Gson gson = new Gson();

  public static String q(Object msg) {
    return gson.toJson(msg);
  }
}
