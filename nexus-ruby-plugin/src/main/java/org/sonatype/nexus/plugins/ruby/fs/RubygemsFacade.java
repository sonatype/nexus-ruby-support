package org.sonatype.nexus.plugins.ruby.fs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
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

    @SuppressWarnings("deprecation")
    InputStream bundlerDependencies( StorageFileItem specs, long modified,
            StorageFileItem prereleasedSpecs, long prereleasedModified,
            File cacheDir, String... gemnames )
                    throws ItemNotFoundException, org.sonatype.nexus.proxy.StorageException, IOException;

}