package org.sonatype.nexus.plugins.ruby.proxy;

import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.plugins.ruby.fs.AbstractRubygemsFacade;
import org.sonatype.nexus.plugins.ruby.fs.RubyLocalRepositoryStorage;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.ruby.RubygemsGateway;
import org.sonatype.nexus.ruby.SpecsIndexType;

public class ProxyRubygemsFacade extends AbstractRubygemsFacade {
    
    public ProxyRubygemsFacade(RubygemsGateway gateway, RubyRepository repository) {
        super(gateway, repository);
    }

    @Override
    public void addGem( RubyLocalRepositoryStorage storage, StorageFileItem gem ) 
    {
        //nothing to do
    }
    
    @Override
    public void removeGem( RubyLocalRepositoryStorage storage, StorageFileItem gem )
    {
        //nothing to do
    }
    
    @Override
    public StorageFileItem retrieveSpecsIndex( RubyLocalRepositoryStorage storage, 
            SpecsIndexType type, boolean gzipped ) 
            throws ItemNotFoundException, LocalStorageException
    {
        return storage.retrieveSpecsIndex( repository, type, gzipped );
    }
}