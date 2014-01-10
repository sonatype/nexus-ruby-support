package org.sonatype.nexus.plugins.ruby.proxy;

import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.plugins.ruby.fs.AbstractRubygemsFacade;
import org.sonatype.nexus.ruby.RubygemsGateway;

public class ProxyRubygemsFacade extends AbstractRubygemsFacade {
    
    public ProxyRubygemsFacade(RubygemsGateway gateway, RubyRepository repository) {
        super(gateway, repository);
    }
}