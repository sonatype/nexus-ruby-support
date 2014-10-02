/*
 * Copyright (c) 2007-2014 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
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
