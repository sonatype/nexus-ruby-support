package org.sonatype.nexus.plugins.ruby.hosted.rest;

import org.sonatype.nexus.plugins.ruby.RubyHostedRepository;
import org.sonatype.nexus.proxy.ResourceStoreRequest;

public class GemStoreRequest
    extends ResourceStoreRequest
{
    private final RubyHostedRepository rubyRepository;

    public GemStoreRequest( RubyHostedRepository repository, String path, boolean localOnly, boolean removeOnly )
    {
        super( path, localOnly, removeOnly );

        this.rubyRepository = repository;
    }

    public GemStoreRequest( RubyHostedRepository repository, String path, boolean localOnly )
    {
        this( repository, path, localOnly, false );
    }
    
    public RubyHostedRepository getRubyHostedRepository()
    {
        return rubyRepository;
    }
}