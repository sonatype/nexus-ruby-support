package org.sonatype.nexus.plugins.ruby.proxy;

import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.plugins.ruby.fs.AbstractRubygemsFacade;
import org.sonatype.nexus.plugins.ruby.fs.RubyLocalRepositoryStorage;
import org.sonatype.nexus.plugins.ruby.fs.RubygemFile;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.ruby.RubygemsGateway;

public class ProxyRubygemsFacade extends AbstractRubygemsFacade {
    
    public ProxyRubygemsFacade(RubygemsGateway gateway, RubyRepository repository) {
        super(gateway, repository);
    }
    
    @Override
    public boolean removeGem( RubyLocalRepositoryStorage storage, StorageFileItem gem )
    {
        return true;
    }

    @Override
    public RubygemFile addGem( RubyLocalRepositoryStorage storage,
                               StorageFileItem gem )
            throws UnsupportedStorageOperationException, LocalStorageException
    {
        // nothing to do and null should be sufficient
        return null;
    }

}