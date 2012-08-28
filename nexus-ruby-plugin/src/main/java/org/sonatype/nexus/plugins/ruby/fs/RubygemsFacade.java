package org.sonatype.nexus.plugins.ruby.fs;

import java.io.IOException;
import java.util.List;

import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;

public interface RubygemsFacade {

    void addGem( RubyLocalRepositoryStorage storage, StorageFileItem gem )
            throws UnsupportedStorageOperationException, LocalStorageException;

    void removeGem( RubyLocalRepositoryStorage storage, StorageFileItem gem )
            throws UnsupportedStorageOperationException, LocalStorageException;

    void mergeSpecsIndex( RubyLocalRepositoryStorage storage,
            SpecsIndexType type, StorageItem localSpecs, List<StorageItem> specsItems )
                    throws UnsupportedStorageOperationException, 
                    LocalStorageException, IOException;

    StorageFileItem retrieveSpecsIndex( RubyLocalRepositoryStorage storage,
            SpecsIndexType type, boolean gzipped )
                    throws ItemNotFoundException, LocalStorageException;

}