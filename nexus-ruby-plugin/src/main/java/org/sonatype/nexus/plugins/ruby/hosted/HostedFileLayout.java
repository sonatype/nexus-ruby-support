package org.sonatype.nexus.plugins.ruby.hosted;

import static org.sonatype.nexus.proxy.ItemNotFoundException.reasonFor;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.plugins.ruby.fs.RubyLocalRepositoryStorage;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.PreparedContentLocator;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.utils.RepositoryStringUtils;
import org.sonatype.nexus.ruby.BundlerApiFile;
import org.sonatype.nexus.ruby.DependencyFile;
import org.sonatype.nexus.ruby.FileLayout;
import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.RubygemsGateway;
import org.sonatype.nexus.ruby.SpecsIndexType;

public class HostedFileLayout extends FileLayout
{
    protected final RubygemsGateway gateway;
    protected final RubyRepository repository;
    
    public HostedFileLayout( RubygemsGateway gateway, RubyRepository repository )
    {
        this.gateway = gateway;
        this.repository = repository;
    }

    public RubygemsFile fromStorageItem( StorageItem item )
    {
        return fromResourceStoreRequestOrNull( item.getResourceStoreRequest() );
    }
        
    public RubygemsFile fromResourceStoreRequest( ResourceStoreRequest request )
            throws ItemNotFoundException
    {
        RubygemsFile file = fromResourceStoreRequestOrNull( request );
        if( file == null )
        {
            throw new ItemNotFoundException( reasonFor( request, repository,
                                                        "Path %s not found in local storage of repository %s", 
                                                        request.getRequestPath(),
                                                        RepositoryStringUtils.getHumanizedNameString( repository ) ) );
        }
        return file;
    }
    
    public RubygemsFile fromResourceStoreRequestOrNull( ResourceStoreRequest request )
    {
        RubygemsFile file = (RubygemsFile) request.getRequestContext().get( RubygemsFile.class.getName() );
        if ( file == null )
        {
            String path = request.getRequestPath();
            // only request with gems=... are used by FileLayout
            if ( request.getRequestUrl() != null && request.getRequestUrl().contains( "?gems=" ) )
            {
                path += request.getRequestUrl().substring( request.getRequestUrl().indexOf( '?' ) );
            }
            file = fromPath( path );
            request.getRequestContext().put( RubygemsFile.class.getName(), file );
        }
        return file;
    }
    
    public ResourceStoreRequest toResourceStoreRequest( RubygemsFile file )
    {
        ResourceStoreRequest request = new ResourceStoreRequest( file.storagePath() );
        request.getRequestContext().put( RubygemsFile.class.getName(), file );
        return request;
    }
    
    @SuppressWarnings( "deprecation" )
    public StorageFileItem createBundlerAPIResponse( BundlerApiFile file )
            throws org.sonatype.nexus.proxy.StorageException, AccessDeniedException,
                   ItemNotFoundException, IllegalOperationException
    {
        List<InputStream> deps = new LinkedList<InputStream>();
        for( String name: file.isBundlerApiFile().gemnames() )
        {
            ResourceStoreRequest req = toResourceStoreRequest(  dependencyFile( name ) );
            try
            {
                deps.add( ((StorageFileItem) repository.retrieveItem( req ) ).getInputStream() );
            }
            catch( IOException e )
            {
                throw new org.sonatype.nexus.proxy.StorageException( e );
            }
        }
        InputStream is = gateway.mergeDependencies( deps );
        
        return ((RubyLocalRepositoryStorage) repository.getLocalStorage()).createTempStorageFile( repository, is, 
                                                                                                  file.type().mime() );
    }

    @SuppressWarnings( "deprecation" )
    public void createDependency( DependencyFile file ) 
            throws org.sonatype.nexus.proxy.StorageException, ItemNotFoundException, IllegalOperationException{
        List<InputStream> gemspecs = new LinkedList<InputStream>();
        try{
            StorageFileItem specs = 
                    (StorageFileItem) repository.retrieveItem( new ResourceStoreRequest( SpecsIndexType.RELEASE.filepathGzipped() ) );
        //    StorageFileItem prereleasedSpecs = 
        //            (StorageFileItem) retrieveItem( new ResourceStoreRequest( SpecsIndexType.PRERELEASE.filepathGzipped() ) );
        
            List<String> versions = gateway.listVersions( file.name(),
                                                          toGZIPInputStream( specs ),
                                                          specs.getModified(),
                                                          false );
            for( String version: versions )
            {
                ResourceStoreRequest req = toResourceStoreRequest( gemspecFile( file.name(), 
                                                                                version ) ); 
                try
                {
                    gemspecs.add( ((StorageFileItem) repository.retrieveItem( req ) ).getInputStream() );
                }
                catch( IOException e )
                {
                    throw new org.sonatype.nexus.proxy.StorageException( e );
                }
            }
        }
        catch( AccessDeniedException e )
        {
            // there was a retrieve before so that should not happen here
            throw new RuntimeException( "BUG" );
        }
        ResourceStoreRequest request = toResourceStoreRequest( file );
        if ( gemspecs.isEmpty() )
        {
            throw new ItemNotFoundException( reasonFor( request, repository,
                                                        "Path %s not found in local storage of repository %s", 
                                                        request.getRequestPath(),
                                                        RepositoryStringUtils.getHumanizedNameString( repository ) ) );

        }
        InputStream is = gateway.createDependencies( gemspecs );
        ContentLocator cl = new PreparedContentLocator( is, file.type().mime(),
                                                        ContentLocator.UNKNOWN_LENGTH );
        DefaultStorageFileItem depsFile = new DefaultStorageFileItem( repository, request,
                                                                      true, true, cl );
    
        try
        {
            repository.storeItem( depsFile );
        }
        catch ( UnsupportedStorageOperationException e )
        {
            // should never happen 
            throw new RuntimeException( "BUG", e );
        }
    }
    

    public InputStream toGZIPInputStream( StorageFileItem item )
            throws LocalStorageException
    {
        try
        {

            if ( item != null )
            {
                return new GZIPInputStream( item.getInputStream() );
            }
            else
            {
                return null;
            }
            
        }
        catch ( IOException e ) {
            throw new LocalStorageException( "error getting stream to: " + item, e );
        }
    }
}