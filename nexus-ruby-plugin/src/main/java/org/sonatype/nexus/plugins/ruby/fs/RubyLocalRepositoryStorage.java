package org.sonatype.nexus.plugins.ruby.fs;

import java.io.InputStream;
import java.util.List;

import org.sonatype.nexus.plugins.ruby.RubyGroupRepository;
import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;
import org.sonatype.nexus.ruby.SpecsIndexType;

public interface RubyLocalRepositoryStorage extends LocalRepositoryStorage
{
    
    StorageFileItem retrieveSpecsIndex( RubyRepository repository, SpecsIndexType type, boolean gzipped ) 
            throws LocalStorageException, ItemNotFoundException;
    
    void storeSpecsIndex( RubyRepository repository, SpecsIndexType type, InputStream content ) 
            throws LocalStorageException, UnsupportedStorageOperationException;

    void storeSpecsIndeces( RubyGroupRepository repository, SpecsIndexType type, List<StorageItem> specsItems)
            throws LocalStorageException, UnsupportedStorageOperationException;
}