package org.sonatype.nexus.plugins.ruby.group;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.repository.AbstractGroupRepositoryConfigurator;

@Component( role = DefaultRubyGroupRepositoryConfigurator.class )
public class DefaultRubyGroupRepositoryConfigurator
    extends AbstractGroupRepositoryConfigurator
{

}
