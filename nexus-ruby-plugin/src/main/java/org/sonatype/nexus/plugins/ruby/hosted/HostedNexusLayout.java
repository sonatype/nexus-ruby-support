package org.sonatype.nexus.plugins.ruby.hosted;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.plugins.ruby.NexusLayout;
import org.sonatype.nexus.plugins.ruby.RubyRepository;
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
import org.sonatype.nexus.ruby.ByteArrayInputStream;
import org.sonatype.nexus.ruby.DefaultLayout;
import org.sonatype.nexus.ruby.DependencyFile;
import org.sonatype.nexus.ruby.GemFile;
import org.sonatype.nexus.ruby.GemspecFile;
import org.sonatype.nexus.ruby.Layout;
import org.sonatype.nexus.ruby.RubygemsGateway;
import org.sonatype.nexus.ruby.SpecsIndexFile;
import org.sonatype.nexus.ruby.SpecsIndexType;

@Singleton
public class HostedNexusLayout extends NexusLayout implements Layout
{
    
    @Inject
    public HostedNexusLayout( //@Named("default") Layout layout,
                              DefaultLayout layout,
                              RubygemsGateway gateway )
    {
        super( layout, gateway );
    }    

    @SuppressWarnings( "deprecation" )
    @Override
    public StorageFileItem retrieveSpecIndex( RubyRepository repository,
                                              SpecsIndexFile specIndex )
            throws org.sonatype.nexus.proxy.StorageException, 
                   AccessDeniedException, IllegalOperationException,
                   ItemNotFoundException
    {
        try
        {
            return super.retrieveSpecIndex( repository, specIndex );
        }
        catch (ItemNotFoundException e)
        {
            createEmptySpecs( repository, specIndex.specsType() );
            return super.retrieveSpecIndex( repository, specIndex );
        }
    }

    @SuppressWarnings( "deprecation" )
    public void createGemspec( RubyRepository repository, 
                               GemspecFile gemspec ) 
            throws org.sonatype.nexus.proxy.StorageException,
                   ItemNotFoundException, IllegalOperationException,
                   AccessDeniedException
    {
        StorageItem gem = repository.retrieveItem( toResourceStoreRequest( gemspec.gem() ) );
        try
        {
            Object spec = gateway.spec( ((StorageFileItem) gem ).getInputStream() );
            storeGemspecRz( repository, spec, gemspec );
        }
        catch ( IOException e )
        {
            throw new  org.sonatype.nexus.proxy.StorageException( e );
        }
        catch ( UnsupportedStorageOperationException e )
        {
            // should never happen 
            throw new RuntimeException( "BUG", e );
        }
    }

    @SuppressWarnings( "deprecation" )
    public void createDependency( RubyRepository repository, 
                                  DependencyFile file ) 
            throws org.sonatype.nexus.proxy.StorageException,
                   ItemNotFoundException, IllegalOperationException,
                   AccessDeniedException
    {
        List<InputStream> gemspecs = new LinkedList<InputStream>();
        try{
            StorageFileItem specs = (StorageFileItem) retrieveSpecsIndex( repository, SpecsIndexType.RELEASE );
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
            try
            {
                repository.deleteItem( false, request );
            }
            catch (UnsupportedStorageOperationException e)
            {
                // delete should work as it is needed elsewhere
                throw new RuntimeException( "BUG" );
            }
        }
        else
        {
            InputStream is = gateway.createDependencies( gemspecs );
            try
            {
                store( repository, is, ContentLocator.UNKNOWN_LENGTH,
                       file.type().mime(), request );
            }
            catch ( UnsupportedStorageOperationException e )
            {
                // should never happen 
                throw new RuntimeException( "BUG", e );
            }
            finally
            {
                IOUtil.close( is );
            }
        }
    }

    @SuppressWarnings( "deprecation" )
    public void deleteGem( RubyRepository repository,
                           GemFile gem ) 
           throws org.sonatype.nexus.proxy.StorageException,
                  UnsupportedStorageOperationException, IllegalOperationException,
                  ItemNotFoundException, AccessDeniedException
    {
        InputStream is = null;
        try
        {
            StorageFileItem file = (StorageFileItem) repository.retrieveItem( toResourceStoreRequest( gem ) );
            is = file.getInputStream();
            deleteSpecToIndex( repository, gateway.spec( is ) );
        }
        catch ( IOException | AccessDeniedException e )
        {
            throw new org.sonatype.nexus.proxy.StorageException( e );
        }
        finally
        {
            IOUtil.close( is );
        }

        createDependency( repository, gem.dependency() );
    }

    public void storeGem( RubyRepository repository,
                          InputStream is )
       throws LocalStorageException,
              UnsupportedStorageOperationException, IllegalOperationException
    {
        File tmpFile = null;
        try
        {
            tmpFile = File.createTempFile( "gems-", ".gem", 
                                           repository.getApplicationTempDirectory() );
            IOUtil.copy( is, new FileOutputStream( tmpFile ) ); 
            Object spec = gateway.spec( new FileInputStream( tmpFile ) );
            GemFile gemFile = gemFile( gateway.gemname( spec ) );

            doStoreGem( repository, gemFile, spec );
          }
          catch (IOException | ItemNotFoundException | AccessDeniedException e)
          {
              throw new LocalStorageException( "error creating temp gem file",
                                               e );
          }
    }

    @SuppressWarnings( "deprecation" )
    public void storeGem( RubyRepository repository,
                          GemFile gem ) 
           throws org.sonatype.nexus.proxy.StorageException,
                  UnsupportedStorageOperationException, IllegalOperationException,
                  AccessDeniedException, ItemNotFoundException
    {
        StorageFileItem file = (StorageFileItem) repository.retrieveItem( toResourceStoreRequest( gem ) );
        Object spec = null;
        try
        {
            spec = gateway.spec( file.getInputStream(),
                                 new File( gem.storagePath() ).getName() );
        }
        catch( IOException e )
        {
            throw new org.sonatype.nexus.proxy.StorageException( e );
        }
        
        doStoreGem( repository, gem, spec );
    }

    @SuppressWarnings( "deprecation" )
    protected void doStoreGem( RubyRepository repository, GemFile gem, Object spec )
            throws org.sonatype.nexus.proxy.StorageException,
                   UnsupportedStorageOperationException,
                   IllegalOperationException, LocalStorageException,
                   AccessDeniedException, ItemNotFoundException
    {
        storeGemspecRz( repository, spec, gem.gemspec() );

        // add the spec to the index
        addSpecToIndex( repository, spec );
        
        // create a new dependency file including the new gem
        createDependency( repository, gem.dependency() );
    }

    @SuppressWarnings( "deprecation" )
    protected void storeGemspecRz( RubyRepository repository, Object spec,
                                 GemspecFile gemspec ) 
        throws org.sonatype.nexus.proxy.StorageException,
               UnsupportedStorageOperationException, IllegalOperationException
    {
        ByteArrayInputStream is = null;
        try
        {
            // store the gemspec.rz
            ResourceStoreRequest request = toResourceStoreRequest( gemspec );
            is = gateway.createGemspecRz( spec );
            store( repository, is, is.length(), gemspec.type().mime(), request );
        }
        finally
        {
            IOUtil.close( is );
        }
    }

    @SuppressWarnings( "deprecation" )
    protected void addSpecToIndex( RubyRepository repository, Object spec )
         throws org.sonatype.nexus.proxy.StorageException,
                LocalStorageException,
                AccessDeniedException, IllegalOperationException,
                UnsupportedStorageOperationException, ItemNotFoundException
    {
        for (SpecsIndexType type : SpecsIndexType.values())
        {
            InputStream in = null;
            InputStream content = null;
            try
            {
                in = retrieveSpecsIndexStream( repository, type );
                content = gateway.addSpec( spec, in, type );
                // if nothing was added the result is NULL
                if ( content != null )
                {
                    storeSpecsIndex( repository, type, content );
                }
            }
            finally
            {
                IOUtil.close( in );
                IOUtil.close( content );
            }
        }
    }

    @SuppressWarnings( "deprecation" )
    protected void deleteSpecToIndex( RubyRepository repository, Object spec )
         throws org.sonatype.nexus.proxy.StorageException,
                LocalStorageException,
                AccessDeniedException, IllegalOperationException,
                UnsupportedStorageOperationException, ItemNotFoundException
    {
        for (SpecsIndexType type : SpecsIndexType.values())
        {
            InputStream in = null;
            InputStream release = null;
            InputStream content = null;
            try
            {
                in = retrieveSpecsIndexStream( repository, type );
                if( type == SpecsIndexType.LATEST )
                {
                    // if we delete the entry from latest we need to use the release to 
                    // recreate the latest index
                    release = retrieveSpecsIndexStream( repository, SpecsIndexType.RELEASE );
                    content = gateway.deleteSpec( spec, in, release );
                }
                else
                {
                    content = gateway.deleteSpec( spec, in );
                }
                // if nothing was added the result is NULL
                if ( content != null )
                {
                    storeSpecsIndex( repository, type, content );
                }
            }
            finally
            {
                IOUtil.close( in );
                IOUtil.close( release );
                IOUtil.close( content );
            }
        }
    }

    @SuppressWarnings( "deprecation" )
    protected void store( RubyRepository repository,
                          InputStream is,
                          long length,
                          String mime,
                          ResourceStoreRequest request )
            throws org.sonatype.nexus.proxy.StorageException,
                   UnsupportedStorageOperationException, IllegalOperationException
    {
        ContentLocator contentLocator = new PreparedContentLocator( is, mime, length );

        DefaultStorageFileItem gemspecFile = new DefaultStorageFileItem( repository,
                                                                         request,
                                                                         true, true,
                                                                         contentLocator );

        repository.storeItem( gemspecFile );
    }

    @SuppressWarnings( "deprecation" )
    private void storeSpecsIndex( RubyRepository repository,
                                  SpecsIndexType type, 
                                  InputStream content )
          throws org.sonatype.nexus.proxy.StorageException,
                 UnsupportedStorageOperationException, IllegalOperationException
    {
        ResourceStoreRequest request = new ResourceStoreRequest( type.filepathGzipped() );
        ByteArrayOutputStream gzipped = new ByteArrayOutputStream();
        GZIPOutputStream out = null;
        try
        {
            out = new GZIPOutputStream( gzipped );
            IOUtil.copy( content, out );
        }
        catch ( IOException e )
        {
            throw new org.sonatype.nexus.proxy.StorageException( e );
        }
        finally
        {
            IOUtil.close( out );
        }
        store( repository, new java.io.ByteArrayInputStream( gzipped.toByteArray() ), 
               gzipped.size(), "application/x-gzip", request );
    }
    
    @SuppressWarnings( "deprecation" )
    private InputStream retrieveSpecsIndexStream( RubyRepository repository,
                                                  SpecsIndexType type )
          throws org.sonatype.nexus.proxy.StorageException,
                 LocalStorageException, AccessDeniedException, 
                 IllegalOperationException, ItemNotFoundException
    {
        return toGZIPInputStream( retrieveSpecsIndex( repository, type ) );
    }
    
    @SuppressWarnings( "deprecation" )
    private StorageFileItem retrieveSpecsIndex( RubyRepository repository,
                                                SpecsIndexType type )
          throws org.sonatype.nexus.proxy.StorageException,
                 LocalStorageException, AccessDeniedException, 
                 IllegalOperationException, ItemNotFoundException
    {
        ResourceStoreRequest req = new ResourceStoreRequest( type.filepathGzipped() );
        StorageFileItem item;
        try
        {
            item = (StorageFileItem) repository.retrieveItem( req );
        }
        catch (ItemNotFoundException e)
        {
            createEmptySpecs( repository, type );
            item = (StorageFileItem) repository.retrieveItem( req );
        }
        return item;
    }
    
    @SuppressWarnings( "deprecation" )
    public void createEmptySpecs( RubyRepository repository, SpecsIndexType type ) 
         throws org.sonatype.nexus.proxy.StorageException,
                IllegalOperationException
    {
        // create an empty index
        try
        {
            storeSpecsIndex( repository, type, gateway.emptyIndex() );
        }
        catch ( UnsupportedStorageOperationException e )
        {
            throw new org.sonatype.nexus.proxy.StorageException( e );
        }
    }
}