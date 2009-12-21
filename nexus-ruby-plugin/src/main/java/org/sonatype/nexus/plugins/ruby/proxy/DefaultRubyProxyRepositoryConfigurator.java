package org.sonatype.nexus.plugins.ruby.proxy;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.repository.AbstractProxyRepositoryConfigurator;

@Component( role = DefaultRubyProxyRepositoryConfigurator.class )
public class DefaultRubyProxyRepositoryConfigurator
    extends AbstractProxyRepositoryConfigurator
{

}
