package org.sonatype.nexus.ruby.cuba;

import org.sonatype.nexus.ruby.RubygemsFile;


public interface Cuba
{
  /**
   * create the RubygemsFile for the given <code>State</code>
   *
   * @return RubygemsFile
   */
  RubygemsFile on(State state);
}