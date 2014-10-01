package org.sonatype.nexus.ruby;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

public class IOUtil
{
  public static void close(InputStream input) {
    if (input == null) {
      return;
    }

    try {
      input.close();
    }
    catch (IOException ex) {
      // ignore
    }
  }

  public static void close(OutputStream output) {
    if (output == null) {
      return;
    }

    try {
      output.close();
    }
    catch (IOException ex) {
      // ignore
    }
  }

  /**
   * Copy bytes from an <code>InputStream</code> to an <code>OutputStream</code>.
   *
   * @param bufferSize Size of internal buffer to use.
   */
  public static void copy(final InputStream input, final OutputStream output) throws IOException {
    final byte[] buffer = new byte[4096];
    int n = 0;
    while (-1 != (n = input.read(buffer))) {
      output.write(buffer, 0, n);
    }
  }

  public static InputStream toGzipped(final InputStream input) throws IOException {
    ByteArrayOutputStream gzipped = new ByteArrayOutputStream();
    GZIPOutputStream out = new GZIPOutputStream(gzipped);
    try {
      copy(input, out);
      out.close();
      return new java.io.ByteArrayInputStream(gzipped.toByteArray());
    }
    finally {
      close(input);
      close(out);
    }
  }
}
