package org.sonatype.nexus.ruby;

public class TestUtils
{
  public static String lastLine(String text) {
    String[] lines = lines(text);
    return lines[lines.length - 1];
  }

  public static String[] lines(String text) {
    return text.split("\\n");
  }

  public static int numberOfLines(String text) {
    return lines(text).length;
  }
}