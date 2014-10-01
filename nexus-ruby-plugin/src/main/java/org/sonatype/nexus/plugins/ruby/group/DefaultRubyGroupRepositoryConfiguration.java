package org.sonatype.nexus.plugins.ruby.group;

import org.sonatype.nexus.proxy.repository.AbstractGroupRepositoryConfiguration;

import org.codehaus.plexus.util.xml.Xpp3Dom;

public class DefaultRubyGroupRepositoryConfiguration
    extends AbstractGroupRepositoryConfiguration
{
  public DefaultRubyGroupRepositoryConfiguration(Xpp3Dom configuration) {
    super(configuration);
  }
}
