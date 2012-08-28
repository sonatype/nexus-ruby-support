package org.sonatype.nexus.plugins.ruby;

import org.sonatype.nexus.plugins.ruby.fs.RubygemsFacade;
import org.sonatype.nexus.proxy.repository.Repository;

public interface RubyRepository
    extends Repository
{

    RubygemsFacade getRubygemsFacade();
}
