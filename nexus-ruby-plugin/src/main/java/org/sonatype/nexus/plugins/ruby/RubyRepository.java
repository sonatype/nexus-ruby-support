package org.sonatype.nexus.plugins.ruby;

import java.io.File;

import org.slf4j.Logger;
import org.sonatype.nexus.plugins.ruby.fs.RubygemFile;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.RemoteAccessException;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;

public interface RubyRepository
    extends Repository
{
    
    File getApplicationTempDirectory();
    
    Logger getLog();
    
    @SuppressWarnings("deprecation")
    StorageItem retrieveJavaGem( RubygemFile gem ) 
            throws RemoteAccessException, AccessDeniedException, 
                   org.sonatype.nexus.proxy.StorageException,
                   IllegalOperationException, ItemNotFoundException;
    
    @SuppressWarnings("deprecation")
    StorageItem retrieveJavaGemspec( RubygemFile gem ) 
            throws RemoteAccessException, AccessDeniedException, 
                   org.sonatype.nexus.proxy.StorageException,
                   IllegalOperationException, ItemNotFoundException;

    @SuppressWarnings("deprecation")
    void storeItem( StorageItem item ) 
            throws UnsupportedStorageOperationException, IllegalOperationException, 
            org.sonatype.nexus.proxy.StorageException;
}
