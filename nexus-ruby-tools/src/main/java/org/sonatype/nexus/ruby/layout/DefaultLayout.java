package org.sonatype.nexus.ruby.layout;

import java.io.InputStream;

import org.sonatype.nexus.ruby.DefaultRubygemsFileFactory;
import org.sonatype.nexus.ruby.RubygemsFile;

/**
 * adds dummy implementation for {@link Layout#addGem(InputStream, RubygemsFile)}
 *
 * @author christian
 */
public class DefaultLayout
    extends DefaultRubygemsFileFactory
    implements Layout
{
  /*
   * (non-Javadoc)
   * @see org.sonatype.nexus.ruby.layout.Layout#addGem(java.io.InputStream, org.sonatype.nexus.ruby.RubygemsFile)
   */
  @Override
  public void addGem(InputStream is, RubygemsFile file) {
    throw new RuntimeException("not implemented !");
  }
}