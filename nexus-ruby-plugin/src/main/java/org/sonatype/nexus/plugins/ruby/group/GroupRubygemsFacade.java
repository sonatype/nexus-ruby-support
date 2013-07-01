package org.sonatype.nexus.plugins.ruby.group;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.plugins.ruby.fs.AbstractRubygemsFacade;
import org.sonatype.nexus.plugins.ruby.fs.RubyLocalRepositoryStorage;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.ruby.BundlerDependencies;
import org.sonatype.nexus.ruby.RubygemsGateway;
import org.sonatype.nexus.ruby.SpecsIndexType;

public class GroupRubygemsFacade extends AbstractRubygemsFacade {

    public GroupRubygemsFacade( RubygemsGateway gateway, RubyRepository repository )
    {
        super( gateway, repository );
    }

    @Override
    public void mergeSpecsIndex( RubyLocalRepositoryStorage storage, SpecsIndexType type,
            StorageItem localItem, List<StorageItem> specsItems )
            throws UnsupportedStorageOperationException, LocalStorageException, IOException {
        List<InputStream> streams = new LinkedList<InputStream>();
        for( StorageItem item: specsItems )
        {
            streams.add( toGZIPInputStream( (StorageFileItem) item ) );
        }
        InputStream is = localItem == null ? null : toGZIPInputStream( (StorageFileItem) localItem );
        storeSpecsIndex( repository, 
                         storage, 
                         type,
                         gateway.mergeSpecs( is, streams, type == SpecsIndexType.LATEST ) );
    }   
  
    @Override
    public BundlerDependencies bundlerDependencies() 
    {
        return gateway.newBundlerDependencies();
    }
}