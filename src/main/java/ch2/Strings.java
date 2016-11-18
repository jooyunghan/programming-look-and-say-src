package ch2;

import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Strings {
  public static String replace(String s,
                               String regex,
                               Function<MatchResult, String> fn) {
    StringBuffer sb = new StringBuffer();
    Matcher m = Pattern.compile(regex).matcher(s);
    while (m.find()) {
      m.appendReplacement(sb, fn.apply(m));
    }
    m.appendTail(sb);
    return sb.toString();
  }
}
