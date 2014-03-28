package org.sonatype.nexus.plugins.ruby;

import static org.sonatype.nexus.proxy.ItemNotFoundException.reasonFor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.codehaus.plexus.util.IOUtil;
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
import org.sonatype.nexus.ruby.ApiV1File;
import org.sonatype.nexus.ruby.BundlerApiFile;
import org.sonatype.nexus.ruby.DependencyFile;
import org.sonatype.nexus.ruby.Directory;
import org.sonatype.nexus.ruby.GemFile;
import org.sonatype.nexus.ruby.GemspecFile;
import org.sonatype.nexus.ruby.Layout;
import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.RubygemsGateway;
import org.sonatype.nexus.ruby.SpecsIndexFile;

public class NexusLayout
{

    protected final RubygemsGateway gateway;
    protected final Layout layout;

    public NexusLayout( Layout layout, 
                        RubygemsGateway gateway )
    {
        this.layout = layout;
        this.gateway = gateway;
    }

    // delegate to layout

    public SpecsIndexFile specsIndex( String path, boolean gzipped )
    {
        return layout.specsIndex( path, gzipped );
    }

    public Directory directory( String path )
    {
        return layout.directory( path );
    }

    public GemFile gemFile( String name, String version )
    {
        return layout.gemFile( name, version );
    }

    public GemFile gemFile( String nameWithVersion )
    {
        return layout.gemFile( nameWithVersion );
    }

    public GemspecFile gemspecFile( String name, String version )
    {
        return layout.gemspecFile( name, version );
    }

    public GemspecFile gemspecFile( String nameWithVersion )
    {
        return layout.gemspecFile( nameWithVersion );
    }

    public DependencyFile dependencyFile( String name )
    {
        return layout.dependencyFile( name );
    }

    public BundlerApiFile bundlerApiFile( String namesCommaSeparated )
    {
        return layout.bundlerApiFile( namesCommaSeparated );
    }

    public ApiV1File apiV1File( String name )
    {
        return layout.apiV1File( name );
    }

    public RubygemsFile fromPath( String path )
    {
        return layout.fromPath( path );
    }

    // nexus specific methods using ruby-repository
    
    public RubygemsFile fromStorageItem( StorageItem item )
    {
        return fromResourceStoreRequestOrNull( item.getResourceStoreRequest() );
    }

    public RubygemsFile fromResourceStoreRequest( RubyRepository repository, 
                                                  ResourceStoreRequest request )
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
    public StorageFileItem createBundlerAPIResponse( RubyRepository repository, 
                                                     BundlerApiFile file )
            throws org.sonatype.nexus.proxy.StorageException,
            AccessDeniedException, ItemNotFoundException,
            IllegalOperationException
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
        ContentLocator cl = new PreparedContentLocator( gateway.mergeDependencies( deps ),
                                                        file.type().mime(), 
                                                        PreparedContentLocator.UNKNOWN_LENGTH );
        DefaultStorageFileItem result =
                new DefaultStorageFileItem( repository, 
                                            toResourceStoreRequest( file ),
                                            true, false, cl );

        return result;
    }    

    protected InputStream toInputStream( StorageFileItem item )
            throws LocalStorageException
    {
        try
        {
    
            if ( item != null )
            {
                return item.getInputStream();
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

    protected InputStream toGZIPInputStream( StorageFileItem item )
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

    @SuppressWarnings( "deprecation" )
    public StorageFileItem retrieveUnzippedSpecsIndex( RubyRepository repository,
                                                       SpecsIndexFile specIndex ) 
          throws ItemNotFoundException, AccessDeniedException,
                 org.sonatype.nexus.proxy.StorageException, IllegalOperationException
    {
        ResourceStoreRequest request = toResourceStoreRequest( specIndex.zippedSpecsIndexFile() );
        StorageFileItem item = (StorageFileItem) repository.retrieveItem( request );
        DefaultStorageFileItem unzippedItem = null;
        try
        {
            unzippedItem = new DefaultStorageFileItem( repository,
                                        toResourceStoreRequest( specIndex.unzippedSpecsIndexFile() ),
                                        true, false,
                                        gunzipContentLocator( item ) );
        }
        catch (IOException e)
        {
            throw new ItemNotFoundException( reasonFor( request, repository,
                                                        "Could not create unzipped content for path %s in local storage of repository %s", 
                                                        request.getRequestPath(),
                                                        RepositoryStringUtils.getHumanizedNameString( repository ) ) );
        }
        unzippedItem.setModified( item.getModified() );
        return unzippedItem;
    } 
    
    private ContentLocator gunzipContentLocator( StorageFileItem item )
            throws IOException
    {
        InputStream in = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
         try {
             in = new GZIPInputStream( item.getInputStream() );
             IOUtil.copy( in, out );
             
             return new PreparedContentLocator( new ByteArrayInputStream( out.toByteArray() ), 
                                                "application/x-marshal-ruby",
                                                out.toByteArray().length );
         }
         finally
         {
             IOUtil.close( in );
             IOUtil.close( out );
         }
    }
    
    protected void storeSpecsIndex( RubyRepository repository, SpecsIndexFile file,
                                    InputStream content)
         throws LocalStorageException, UnsupportedStorageOperationException,
                IllegalOperationException
    {
        OutputStream out = null;
        try
        {
            ByteArrayOutputStream gzipped = new ByteArrayOutputStream();
            out = new GZIPOutputStream( gzipped );
            IOUtil.copy( content, out );
            // need to close gzip stream here
            out.close();
            ContentLocator cl = new PreparedContentLocator( new ByteArrayInputStream( gzipped.toByteArray() ),
                                                            "application/x-gzip",
                                                            gzipped.size() );
            DefaultStorageFileItem item =
                    new DefaultStorageFileItem( repository,
                                                toResourceStoreRequest( file ),
                                                true, true, cl );
            repository.storeItem( item );
        }
        catch ( IOException e )
        {
            new LocalStorageException( "error storing: " + file, e );
        }
        finally
        {
            IOUtil.close( content );
            IOUtil.close( out );
        }
    }
}