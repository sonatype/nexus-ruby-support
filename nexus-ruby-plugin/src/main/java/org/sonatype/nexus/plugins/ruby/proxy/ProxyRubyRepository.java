package org.sonatype.nexus.plugins.ruby.proxy;

import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.RemoteAccessException;
import org.sonatype.nexus.proxy.repository.ProxyRepository;


public interface ProxyRubyRepository
    extends RubyRepository, ProxyRepository
{

    int getArtifactMaxAge();

    void setArtifactMaxAge( int maxAge );

    int getMetadataMaxAge();

    void setMetadataMaxAge( int metadataMaxAge );
    
    @SuppressWarnings( "deprecation" )
    void syncMetadata( ) throws LocalStorageException, ItemNotFoundException, 
        RemoteAccessException, AccessDeniedException, org.sonatype.nexus.proxy.StorageException, 
        IllegalOperationException, NoSuchResourceStoreException;
}
