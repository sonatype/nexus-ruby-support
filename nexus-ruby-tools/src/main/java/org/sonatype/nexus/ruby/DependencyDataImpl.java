package org.sonatype.nexus.ruby;

import org.jruby.embed.ScriptingContainer;

/**
 * a wrapper around a JRuby object
 *
 * @author christian
 */
public class DependencyDataImpl
    extends ScriptWrapper
    implements DependencyData
{
  private final long modified;

  public DependencyDataImpl(ScriptingContainer scriptingContainer,
                            Object dependencies, long modified)
  {
    super(scriptingContainer, dependencies);
    this.modified = modified;
  }

  /* (non-Javadoc)
   * @see org.sonatype.nexus.ruby.Dependencies#versions(boolean)
   */
  @Override
  public String[] versions(boolean prereleased) {
    return callMethod("versions", prereleased, String[].class);
  }

  /* (non-Javadoc)
   * @see org.sonatype.nexus.ruby.Dependencies#platform(java.lang.String)
   */
  @Override
  public String platform(String version) {
    return callMethod("platform", version, String.class);
  }

  /* (non-Javadoc)
   * @see org.sonatype.nexus.ruby.Dependencies#name()
   */
  @Override
  public String name() {
    return callMethod("name", String.class);
  }

  /* (non-Javadoc)
   * @see org.sonatype.nexus.ruby.Dependencies#modified()
   */
  @Override
  public long modified() {
    return modified;
  }

  @Override
  public String toString() {
    return callMethod("inspect", String.class);
  }
}