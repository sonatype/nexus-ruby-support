package org.sonatype.nexus.plugins.ruby;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.zip.GZIPInputStream;

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
import org.sonatype.nexus.proxy.utils.RepositoryStringUtils;
import org.sonatype.nexus.ruby.IOUtil;
import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.SpecsIndexFile;
import org.sonatype.nexus.ruby.layout.Storage;

public class NexusStorage implements Storage
{

    private final RubyRepository repository;
    
    public NexusStorage( RubyRepository repository )
    {
        this.repository = repository;
    }
    
    public ItemNotFoundException newNotFoundException( String path ){
        return new ItemNotFoundException( ItemNotFoundException.reasonFor( new ResourceStoreRequest( path ), this.repository,
                                                                           "Can not serve path %s for repository %s", path,
                                                                           RepositoryStringUtils.getHumanizedNameString( this.repository ) ) );
    }

    @Override
    public boolean retrieve( RubygemsFile file )
    {
        try
        {
            file.set( repository.retrieveDirectItem( new ResourceStoreRequest( file.storagePath() ) ) );
            return true;
        }
        catch ( ItemNotFoundException e )
        {
            file.markAsNotExists();
            return false;
        }
        catch ( IOException | AccessDeniedException | IllegalOperationException e )
        {
            file.setException( e );
            return false;
        }
    }

    @Override
    public boolean retrieveUnzippped( SpecsIndexFile specs )
    {
        try
        {
            StorageFileItem item = repository.retrieveDirectItem( new ResourceStoreRequest( specs.zippedSpecsIndexFile().storagePath() ) );
            DefaultStorageFileItem unzippedItem =
                    new DefaultStorageFileItem( repository,
                                                new ResourceStoreRequest( specs.storagePath() ),
                                                true, false,
                                                gunzipContentLocator( item ) );
            unzippedItem.setModified( item.getModified() );
            specs.set( unzippedItem );
            return true;
        }
        catch ( IOException | AccessDeniedException | IllegalOperationException | ItemNotFoundException e )
        {
            specs.setException( e );
            return false;
        }
    }
    
    private ContentLocator gunzipContentLocator( StorageFileItem item )
            throws IOException
    {
        InputStream in = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
         try
         {
             in = new GZIPInputStream( item.getInputStream() );
             IOUtil.copy( in, out );
             
             return newPreparedContentLocator( new ByteArrayInputStream( out.toByteArray() ), 
                                               "application/x-marshal-ruby",
                                               out.toByteArray().length );
         }
         finally
         {
             IOUtil.close( in );
             IOUtil.close( out );
         }
    }

    @Override
    public InputStream getInputStream( RubygemsFile file ) throws IOException
    {
        if ( file.get() == null )
        {
            retrieve( file );
        }
        return ((StorageFileItem) file.get() ).getInputStream();
    }

    @Override
    public long getModified( RubygemsFile file )
    {
        return ((StorageItem) file.get() ).getModified();
    }
        
    @Override
    public boolean create( InputStream is, RubygemsFile file ) 
    {
        return update( is, file );
    }
    
    @Override
    public boolean update( InputStream is, RubygemsFile file ) 
    {
        file.resetState();
        ResourceStoreRequest request = new ResourceStoreRequest( file.storagePath() );
        ContentLocator contentLocator = newPreparedContentLocator( is, file.type().mime(), ContentLocator.UNKNOWN_LENGTH );        
        DefaultStorageFileItem fileItem = new DefaultStorageFileItem( repository, request,
                                                                      true, true, contentLocator );

        try
        {
            // we need to bypass access control here !!!
            repository.storeItem( false, fileItem );
            return true;
        }
        catch ( IOException | UnsupportedStorageOperationException | IllegalOperationException e )
        {
            file.setException( e );
            return false;
        }
    }

    private ContentLocator newPreparedContentLocator( InputStream is, String mime, long length )
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

    @SuppressWarnings( "deprecation" )
    public boolean delete( RubygemsFile file ) 
    {
        ResourceStoreRequest request = new ResourceStoreRequest( file.storagePath() );

        try
        {
            repository.deleteItem( false, request );
            return true;
        }
        catch ( IOException | UnsupportedStorageOperationException | IllegalOperationException e )
        {
            file.setException( e );
            return false;
        }
        catch (ItemNotFoundException e)
        {
            // already deleted
            return true;
        }
    }

    @Override
    public void memory( InputStream data, RubygemsFile file )
    {
        memory( data, file, ContentLocator.UNKNOWN_LENGTH );
    }

    @Override
    public void memory( String data, RubygemsFile file )
    {
        memory( new ByteArrayInputStream( data.getBytes() ), file, data.getBytes().length );
        
    }
    
    private void memory( InputStream data, RubygemsFile file, long length )
    {
        ContentLocator cl = newPreparedContentLocator( data, file.type().mime(), length );
        file.set( new DefaultStorageFileItem( repository, new ResourceStoreRequest( file.storagePath() ),
                                              true, false, cl ) );
    }
}
