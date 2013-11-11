package org.sonatype.nexus.plugins.ruby.fs;

import java.io.IOException;
import java.util.List;

import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.ruby.BundlerDependencies;
import org.sonatype.nexus.ruby.SpecsIndexType;

public interface RubygemsFacade {

    void addGem( RubyLocalRepositoryStorage storage, StorageFileItem gem )
            throws UnsupportedStorageOperationException, LocalStorageException;

    boolean removeGem( RubyLocalRepositoryStorage storage, StorageFileItem gem )
            throws UnsupportedStorageOperationException, LocalStorageException;

    void mergeSpecsIndex( RubyLocalRepositoryStorage storage,
            SpecsIndexType type, StorageItem localSpecs, List<StorageItem> specsItems )
                    throws UnsupportedStorageOperationException, 
                    LocalStorageException, IOException;

    RubygemFile deletableFile( String path );

    StorageItem retrieveItem( RubyLocalRepositoryStorage storage, 
                                  ResourceStoreRequest request ) 
        throws AccessDeniedException, StorageException, ItemNotFoundException, IllegalOperationException;
    
    @SuppressWarnings("deprecation")
    BundlerDependencies bundlerDependencies() 
            throws LocalStorageException, AccessDeniedException, ItemNotFoundException, 
                    IllegalOperationException, org.sonatype.nexus.proxy.StorageException;

    @SuppressWarnings("deprecation")
    StorageFileItem[] prepareDependencies(BundlerDependencies bundler, String... gemnames)
            throws ItemNotFoundException, AccessDeniedException,
            IllegalOperationException, org.sonatype.nexus.proxy.StorageException;

}