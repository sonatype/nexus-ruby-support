package org.sonatype.nexus.plugins.ruby.hosted;

import org.sonatype.nexus.proxy.repository.AbstractRepositoryConfiguration;

import org.codehaus.plexus.util.xml.Xpp3Dom;

public class DefaultHostedRubyRepositoryConfiguration
    extends AbstractRepositoryConfiguration
{
  public DefaultHostedRubyRepositoryConfiguration(Xpp3Dom configuration) {
    super(configuration);
  }
}
