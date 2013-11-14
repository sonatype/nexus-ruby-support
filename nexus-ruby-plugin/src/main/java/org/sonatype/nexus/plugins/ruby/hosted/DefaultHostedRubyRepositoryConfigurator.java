package org.sonatype.nexus.plugins.ruby.hosted;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.repository.AbstractRepositoryConfigurator;

@Component( role = DefaultHostedRubyRepositoryConfigurator.class )
public class DefaultHostedRubyRepositoryConfigurator
    extends AbstractRepositoryConfigurator
{

}
