package org.sonatype.nexus.plugins.ruby.hosted;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.zip.GZIPInputStream;

import org.codehaus.plexus.util.IOUtil;
import org.jsoup.Connection.Request;
import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.PreparedContentLocator;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.ruby.DependencyFile;
import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.RubygemsGateway;
import org.sonatype.nexus.ruby.SpecsIndexFile;
import org.sonatype.nexus.ruby.layout.HostedGETLayout;

public class NexusHostedGETLayout extends HostedGETLayout
{

    final RubyRepository repository;
    
    public NexusHostedGETLayout( RubygemsGateway gateway, RubyRepository repository )
    {
        super( gateway );
        this.repository = repository;
    }
    
    public RubygemsFile fromPath( ResourceStoreRequest request )
    {   
        String path = request.getRequestPath();
        // only request with gems=... are used by FileLayout
        if ( request.getRequestUrl() != null && request.getRequestUrl().contains( "?gems=" ) )
        {
            path += request.getRequestUrl().substring( request.getRequestUrl().indexOf( '?' ) );
        }
        return fromPath( path );
    }
    
    protected void retrieve( RubygemsFile file )
    {
        try
        {
            file.set( repository.retrieveDirectItem( new ResourceStoreRequest( file.storagePath() ) ) );
        }
        catch ( @SuppressWarnings( "deprecation" ) org.sonatype.nexus.proxy.StorageException
               | AccessDeniedException | IllegalOperationException | ItemNotFoundException e )
        {
            file.setException( e );
        }
    }

    @Override
    public InputStream getInputStream( RubygemsFile file ) throws IOException
    {
        return ((StorageFileItem) file.get() ).getInputStream();
    }

    @Override
    protected long getModified( RubygemsFile file )
    {
        return ((StorageItem) file.get() ).getModified();
    }
    
    protected void setMemoryContext( RubygemsFile file, InputStream data, long length )
    {
        ContentLocator cl = newPreparedContentLocator( data, file.type().mime(), length );
        file.set( new DefaultStorageFileItem( repository, new ResourceStoreRequest( file.storagePath() ),
                                              true, false, cl ) );
    }

    protected ContentLocator newPreparedContentLocator( InputStream is, String mime, long length )
    {
        try
        {
            return new PreparedContentLocator( is, mime, length );
        }
        catch( NoSuchMethodError e )
        {
            try
            {
                Constructor<PreparedContentLocator> c = PreparedContentLocator.class.getConstructor( new Class[] { InputStream.class, String.class } );
                return c.newInstance( is, mime );
            }
            catch (Exception ee)
            {
                ee.printStackTrace();
                throw e;
            }
        }
    }

    protected void store( InputStream is, RubygemsFile file ) 
    {
        ResourceStoreRequest request = new ResourceStoreRequest( file.storagePath() );
        ContentLocator contentLocator = newPreparedContentLocator( is, file.type().mime(), ContentLocator.UNKNOWN_LENGTH );        
        DefaultStorageFileItem fileItem = new DefaultStorageFileItem( repository, request,
                                                                      true, true, contentLocator );

        try
        {
            repository.storeItem( fileItem );
        }
        catch ( @SuppressWarnings( "deprecation" ) org.sonatype.nexus.proxy.StorageException
                | UnsupportedStorageOperationException | IllegalOperationException e )
        {
            file.setException( e );
        }
    }
    
    @SuppressWarnings( "deprecation" )
    protected void delete( RubygemsFile file ) 
    {
        ResourceStoreRequest request = new ResourceStoreRequest( file.storagePath() );

        try
        {
            repository.deleteItem( false, request );
        }
        catch ( org.sonatype.nexus.proxy.StorageException
                | UnsupportedStorageOperationException | IllegalOperationException | ItemNotFoundException e )
        {
            file.setException( e );
        }
    }

    protected void setGunzippedContext( SpecsIndexFile specs )
    {
        SpecsIndexFile zipped = specs.zippedSpecsIndexFile();
        StorageFileItem item = (StorageFileItem) zipped.get();
        try
        {
            DefaultStorageFileItem unzippedItem =
                    new DefaultStorageFileItem( repository,
                                                new ResourceStoreRequest( specs.storagePath() ),
                                                true, false,
                                                gunzipContentLocator( item ) );
            unzippedItem.setModified( item.getModified() );
            specs.set( unzippedItem );
        }
        catch (IOException e)
        {
            specs.setException( e );
        }
    }
        
    ContentLocator gunzipContentLocator( StorageFileItem item )
            throws IOException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try( InputStream in = new GZIPInputStream( item.getInputStream() ) )
        {
            IOUtil.copy( in, out );
             
            return newPreparedContentLocator( new java.io.ByteArrayInputStream( out.toByteArray() ), 
                                              "application/x-marshal-ruby",
                                              out.toByteArray().length );
         }
         finally
         {
             IOUtil.close( out );
         }
    }
}
