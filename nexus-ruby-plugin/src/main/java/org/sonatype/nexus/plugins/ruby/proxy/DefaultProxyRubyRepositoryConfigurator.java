package org.sonatype.nexus.plugins.ruby.proxy;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.repository.AbstractProxyRepositoryConfigurator;

@Component( role = DefaultProxyRubyRepositoryConfigurator.class )
public class DefaultProxyRubyRepositoryConfigurator
    extends AbstractProxyRepositoryConfigurator
{

}
