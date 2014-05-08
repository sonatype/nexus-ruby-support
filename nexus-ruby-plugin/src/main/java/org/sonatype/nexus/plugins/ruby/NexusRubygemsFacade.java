package org.sonatype.nexus.plugins.ruby;

import java.io.IOException;
import java.io.InputStream;

import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.RemoteAccessException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.utils.RepositoryStringUtils;
import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.cuba.RubygemsFileSystem;

public class NexusRubygemsFacade  
{
    
    private final RubygemsFileSystem filesystem;

    public NexusRubygemsFacade( RubygemsFileSystem filesystem )
    {
        this.filesystem = filesystem;
    } 
    
    public RubygemsFile get( ResourceStoreRequest request )
    {   
        return filesystem.get( request.getRequestPath(), getQuery( request ) );
    }

    protected String getQuery( ResourceStoreRequest request )
    {
        String query = "";
        // only request with ...?gems=... are used by the Layout
        if ( request.getRequestUrl() != null && request.getRequestUrl().contains( "?gems=" ) )
        {
            query = request.getRequestUrl().substring( request.getRequestUrl().indexOf( '?' ) + 1 );
        }
        return query;
    }

    public RubygemsFile file( ResourceStoreRequest request )
    {
        if( request.getRequestPath().contains( "?gems=" ) )
        {
            int index = request.getRequestPath().indexOf( '?' );
            return filesystem.file( request.getRequestPath().substring( 0, index ), 
                                    request.getRequestPath().substring( index + 1 ) );
        }
        return filesystem.file( request.getRequestPath(), getQuery( request ) );
    }

    public RubygemsFile file( String path )
    {
        return filesystem.file( path );
    }

    public RubygemsFile post( InputStream is, String path )
    {
        return filesystem.post( is, path );
    }
    
    public RubygemsFile post( InputStream is, RubygemsFile file )
    {
        filesystem.post( is, file );
        return file;
    }

    public RubygemsFile delete( String original )
    {
        return filesystem.delete( original );
    }
    
    @SuppressWarnings( "deprecation" )
    public StorageItem handleCommon( RubyRepository repository, RubygemsFile file ) 
        throws IllegalOperationException, org.sonatype.nexus.proxy.StorageException
    {
        switch( file.state() )
        {
        case ERROR:
            Exception e = file.getException();
            if ( e instanceof IllegalOperationException )
            {
                throw (IllegalOperationException) e;
            }
            if ( e instanceof RemoteAccessException )
            {
                throw (RemoteAccessException) e;
            }
            if ( e instanceof org.sonatype.nexus.proxy.StorageException )
            {
                throw (org.sonatype.nexus.proxy.StorageException) e;
            }
            if ( e instanceof IOException )
            {
                throw new org.sonatype.nexus.proxy.StorageException( (IOException) e );
            }
            throw new RuntimeException( e );
        case PAYLOAD:
            return (StorageItem) file.get();
        case FORBIDDEN:
        case NOT_EXISTS:
        case TEMP_UNAVAILABLE:
        case NEW_INSTANCE:
        default:
            throw new RuntimeException( "BUG: should not come here - " + file.state() );            
        }
    }
    @SuppressWarnings( "deprecation" )
    public StorageItem handleMutation( RubyRepository repository, RubygemsFile file ) 
        throws IllegalOperationException, org.sonatype.nexus.proxy.StorageException,
               UnsupportedStorageOperationException
    {
        switch( file.state() )
        {
        case ERROR:
            Exception e = file.getException();
            if (  e instanceof UnsupportedStorageOperationException )
            {
                throw new UnsupportedStorageOperationException( file.storagePath() );
            }
        default:
            return handleCommon( repository, file );            
        }
    }

    @SuppressWarnings( "deprecation" )
    public StorageItem handleRetrieve( RubyRepository repository, ResourceStoreRequest req, RubygemsFile file ) 
        throws IllegalOperationException, org.sonatype.nexus.proxy.StorageException,
               ItemNotFoundException
    {
        switch( file.state() )
        {     
        case NO_PAYLOAD:
            // handle directories
            req.setRequestPath( file.storagePath() );
            return repository.retrieveDirectItem( req );
        case NOT_EXISTS:
            throw new ItemNotFoundException( ItemNotFoundException.reasonFor( new ResourceStoreRequest( file.remotePath() ), repository,
                                                                              "Can not serve path %s for repository %s", file.storagePath(),
                                                                              RepositoryStringUtils.getHumanizedNameString( repository ) ) );
        case ERROR:
            Exception e = file.getException();
            if (  e instanceof ItemNotFoundException )
            {
                throw (ItemNotFoundException) e;
            }
        default:
            return handleCommon( repository, file );            
        }
    }
}