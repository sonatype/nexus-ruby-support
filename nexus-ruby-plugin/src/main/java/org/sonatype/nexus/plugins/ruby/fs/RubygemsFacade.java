package org.sonatype.nexus.plugins.ruby.fs;

import java.io.File;

import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.RemoteAccessException;
import org.sonatype.nexus.proxy.item.StorageItem;

public interface RubygemsFacade {

    void setupNewRepo( File basedir ) throws LocalStorageException, ItemNotFoundException;

    @SuppressWarnings( "deprecation" )
    StorageItem retrieveJavaGem( RubyRepository repository, RubygemFile gem )
            throws AccessDeniedException, IllegalOperationException,
                   ItemNotFoundException, RemoteAccessException,
                   org.sonatype.nexus.proxy.StorageException;

    @SuppressWarnings( "deprecation" )
    StorageItem retrieveJavaGemspec( RubyRepository repository, RubygemFile gem )
            throws AccessDeniedException, IllegalOperationException,
                   ItemNotFoundException, RemoteAccessException,
                   org.sonatype.nexus.proxy.StorageException;

}