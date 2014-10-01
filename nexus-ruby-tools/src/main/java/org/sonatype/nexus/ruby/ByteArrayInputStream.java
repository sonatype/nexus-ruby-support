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