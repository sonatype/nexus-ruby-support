package org.sonatype.nexus.plugins.ruby;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;

import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.PreparedContentLocator;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.ruby.DependencyFile;
import org.sonatype.nexus.ruby.Directory;
import org.sonatype.nexus.ruby.IOUtil;
import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.SpecsIndexFile;
import org.sonatype.nexus.ruby.SpecsIndexZippedFile;
import org.sonatype.nexus.ruby.layout.Storage;

public class NexusStorage implements Storage
{

    protected final RubyRepository repository;
    
    public NexusStorage( RubyRepository repository )
    {
        this.repository = repository;
    }

    @Override
    public void retrieve( RubygemsFile file )
    {
        try
        {
            file.set( repository.retrieveDirectItem( new ResourceStoreRequest( file.storagePath() ) ) );
        }
        catch ( ItemNotFoundException e )
        {
            file.markAsNotExists();
        }
        catch ( IOException | IllegalOperationException e )
        {
            file.setException( e );
        }
    }

    @Override
    public void retrieve( DependencyFile file )
    {
        retrieve( (RubygemsFile) file );
    }
    
    @Override
    public void retrieve( SpecsIndexZippedFile file )
    {
        retrieve( (RubygemsFile) file );
    }

    @Override
    public void retrieve( SpecsIndexFile specs )
    {
        SpecsIndexZippedFile source = specs.zippedSpecsIndexFile();
        try
        {
            StorageFileItem item = (StorageFileItem) repository.retrieveDirectItem( new ResourceStoreRequest( source.storagePath() ) );
            DefaultStorageFileItem unzippedItem =
                    new DefaultStorageFileItem( repository,
                                                new ResourceStoreRequest( specs.storagePath() ),
                                                true, false,
                                                gunzipContentLocator( item ) );
            unzippedItem.setModified( item.getModified() );
            specs.set( unzippedItem );
        }
        catch ( ItemNotFoundException e )
        {
            specs.markAsNotExists();
        }
        catch ( IOException | IllegalOperationException e )
        {
            specs.setException( e );
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
    public void create( InputStream is, RubygemsFile file ) 
    {
        update( is, file );
    }
    
    @Override
    public void update( InputStream is, RubygemsFile file ) 
    {
        ResourceStoreRequest request = new ResourceStoreRequest( file.storagePath() );
        ContentLocator contentLocator = newPreparedContentLocator( is, file.type().mime(), ContentLocator.UNKNOWN_LENGTH );        
        DefaultStorageFileItem fileItem = new DefaultStorageFileItem( repository, request,
                                                                      true, true, contentLocator );

        try
        {
            // we need to bypass access control here !!!
            repository.storeItem( false, fileItem );
            file.set( fileItem );
        }
        catch ( IOException | UnsupportedStorageOperationException | IllegalOperationException e )
        {
            file.setException( e );
        }
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

    @SuppressWarnings( "deprecation" )
    public void delete( RubygemsFile file ) 
    {
        ResourceStoreRequest request = new ResourceStoreRequest( file.storagePath() );

        try
        {
            repository.deleteItem( false, request );
        }
        catch ( IOException | UnsupportedStorageOperationException | IllegalOperationException e )
        {
            file.setException( e );
        }
        catch (ItemNotFoundException e)
        {
            // already deleted
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

    @Override
    public String[] listDirectory( Directory dir )
    {
        Set<String> result = new TreeSet<>( Arrays.asList( dir.getItems() ) ); 
        try
        {
            StorageItem list = repository.retrieveDirectItem( new ResourceStoreRequest( dir.storagePath() ) );
            if ( list instanceof StorageCollectionItem )
            {
                for ( StorageItem item : ((StorageCollectionItem) list ).list() ) 
                {
                    result.add( item.getName() );
                }
            }
        }
        catch ( IOException
                | IllegalOperationException | ItemNotFoundException | AccessDeniedException | NoSuchResourceStoreException e )
        {
        }
        return result.toArray( new String[ result.size() ] );
    }
}
