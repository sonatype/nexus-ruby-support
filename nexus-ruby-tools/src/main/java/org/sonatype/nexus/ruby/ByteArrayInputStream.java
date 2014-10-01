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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * jruby's <code>Marshal.dump( v ).bytes.to_a</code> delivers <code>List</code> of <code>Long</code>
 * this InputStream wraps a given List of Long.
 *
 * @author christian
 */
public class ByteArrayInputStream
    extends InputStream
{
  private List<Long> bytes;

  private int cursor = 0;

  public ByteArrayInputStream(List<Long> bytes) {
    this.bytes = bytes;
  }

  /*
   * (non-Javadoc)
   * @see java.io.InputStream#available()
   */
  @Override
  public int available() throws IOException {
    return bytes.size() - cursor;
  }

  /*
   * (non-Javadoc)
   * @see java.io.InputStream#reset()
   */
  @Override
  public void reset() throws IOException {
    cursor = 0;
  }

  /*
   * (non-Javadoc)
   * @see java.io.InputStream#read()
   */
  @Override
  public int read() throws IOException {
    if (cursor < bytes.size()) {
      return bytes.get(cursor++).intValue();
    }
    else {
      return -1;
    }
  }
}