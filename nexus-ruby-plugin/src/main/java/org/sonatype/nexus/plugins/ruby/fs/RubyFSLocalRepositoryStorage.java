package org.sonatype.nexus.plugins.ruby.fs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.mime.MimeSupport;
import org.sonatype.nexus.plugins.ruby.RubyGroupRepository;
import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.DefaultStorageCollectionItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.LinkPersister;
import org.sonatype.nexus.proxy.item.PreparedContentLocator;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.local.fs.DefaultFSLocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.local.fs.FSPeer;
import org.sonatype.nexus.proxy.storage.local.fs.FileContentLocator;
import org.sonatype.nexus.proxy.wastebasket.Wastebasket;

@Component( role = LocalRepositoryStorage.class, hint = DefaultFSLocalRepositoryStorage.PROVIDER_STRING )
public class RubyFSLocalRepositoryStorage extends DefaultFSLocalRepositoryStorage implements RubyLocalRepositoryStorage
{

    @Inject
    public RubyFSLocalRepositoryStorage(Wastebasket wastebasket,
            LinkPersister linkPersister, MimeSupport mimeSupport, FSPeer fsPeer)
    {
        super( wastebasket, linkPersister, mimeSupport, fsPeer );
    }
        
    private ResourceStoreRequest fixPath(ResourceStoreRequest request)
    {
        // put the gems into subdirectory with first-letter of the gems name
        request.setRequestPath( GemFile.fixPath( request.getRequestPath() ) );
        return request;
    }

    
    @Override
    public Collection<StorageItem> listItems(Repository repository,
            ResourceStoreRequest request) throws ItemNotFoundException,
            LocalStorageException {
        final RubyRepository rubyRepository = repository.adaptToFacet( RubyRepository.class );
        if ( rubyRepository != null ) 
        {
            if ( request.getRequestPath().equals( "/" ) ) 
            {
        
                Collection<StorageItem> result = new ArrayList<StorageItem>( 8 );
                          
                result.add( newCollection( repository, "/gems" ) );
                result.add( newCollection( repository, "/quick" ) );
                result.add( newItem( repository, "/latest_specs.4.8" ) );
                result.add( newItem( repository, "/latest_specs.4.8.gz" ) );
                result.add( newItem( repository, "/prerelease_specs.4.8" ) );
                result.add( newItem( repository, "/prerelease_specs.4.8.gz" ) );
                result.add( newItem( repository, "/specs.4.8" ) );
                result.add( newItem( repository, "/specs.4.8.gz" ) );
                        
                return result;
                
            }
            if ( request.getRequestPath().equals( "/quick/" ) ) 
            {
                
                return Arrays.asList( newCollection( repository, "/Marshal.4.8" ) );
                
            }
        }
        return super.listItems(repository, request);
    }

    private StorageItem newItem( Repository repository, String name )
    {
        return new DefaultStorageFileItem( repository, new ResourceStoreRequest( name ), true, false, null );
    }

    private StorageItem newCollection( Repository repository, String name )
    {
        return new DefaultStorageCollectionItem( repository, new ResourceStoreRequest( name ), true, false );
    }

    @Override
    protected AbstractStorageItem retrieveItemFromFile(Repository repository,
            ResourceStoreRequest request, File target)
            throws ItemNotFoundException, LocalStorageException
    {
        final RubyRepository rubyRepository = repository.adaptToFacet( RubyRepository.class );
        if ( rubyRepository != null ) 
        {
            if ( request.getRequestPath().matches( "^\\/quick\\/?$" ) ) 
            {
        
                return new QuickMarshalStorageCollectionItem( rubyRepository, 
                        request, 
                        Arrays.asList(newCollection( repository, "/Marshal.4.8" ) ) );

            }

            if ( request.getRequestPath().matches( "^\\/quick\\/Marshal.4.8\\/?$" ) ) 
            {
                
                File gems = new File( target.getAbsolutePath().replaceFirst( "\\/quick\\/Marshal.4.8\\/?$", "/gems/" ) );
                final Collection<StorageItem> result = new ArrayList<StorageItem>();
                if (gems.exists() )
                {
                    for( File entry: gems.listFiles() )
                    {
                        if ( entry.isDirectory() )
                        {
                            result.add( newCollection( repository, entry.getName() ) );
                        }
                        else if ( entry.isFile() )
                        {
                            result.add( newItem( repository, entry.getName() + "spec.rz" ) );
                        }
                    }
                }
                
                return new QuickMarshalStorageCollectionItem( rubyRepository, request, result );
            }
            if ( request.getRequestPath().matches("/gems/[^/]+\\.gem$") )
            {
                
                fixPath(request);
                // each gems lives in sub-directory with starts with the first letter of the gem-name
                target = new GemFile(target);
                
            }
            else if ( request.getRequestPath().matches("^/quick/.+\\.gemspec.rz$") )
            {
                
                DefaultStorageFileItem file = new DefaultStorageFileItem( repository, request, true, true,
                        new FileContentLocator( target, "application/x-ruby-marshal" ) );
                //repository.getAttributesHandler().fetchAttributes( file );
                file.setContentGeneratorId( GemspecRzContentGenerator.ID );
                return file;
                
            }
        }

        return super.retrieveItemFromFile(repository, request, target);
    }

    @Override
    public boolean containsItem(Repository repository,
            ResourceStoreRequest request) throws LocalStorageException
    {
        return super.containsItem( repository, fixPath( request ) );
    }

    @Override
    public AbstractStorageItem retrieveItem( Repository repository,
            ResourceStoreRequest request ) throws ItemNotFoundException,
            LocalStorageException 
    {
        RubyRepository rubyRepository = repository.adaptToFacet( RubyRepository.class );
        if ( rubyRepository != null ) 
        {
            SpecsIndexType type = SpecsIndexType.fromFilename( request.getRequestPath() );
            if ( type != null )
            {
                if ( request.getRequestPath().startsWith( "/.nexus/" ) )
                {      
                    return super.retrieveItem( repository, request );
                }
                else
                {
                    return (AbstractStorageItem) rubyRepository.getRubygemsFacade().retrieveSpecsIndex( this, type, 
                            request.getRequestPath().endsWith( ".gz" ) );
                    
                }
            }
        }
        return super.retrieveItem( repository, fixPath( request ) );
    }

    @Override
    public void storeItem( Repository repository, StorageItem item )
            throws UnsupportedStorageOperationException, LocalStorageException
    {
        RubyRepository rubyRepository = repository.adaptToFacet( RubyRepository.class );
        if ( rubyRepository != null ) 
        {
            if ( GemFile.isGem( item.getPath() ) )
            {
                ((AbstractStorageItem) item).setPath( GemFile.fixPath( item.getPath() ) );
                item.getResourceStoreRequest().setRequestPath( item.getPath() );

                super.storeItem( repository, item );

                if ( !item.getPath().startsWith("/.nexus" ) )
                {
                    // add it to the index files
                    File gem = getFileFromBase( rubyRepository, item.getResourceStoreRequest() );
                    FileContentLocator locator = new FileContentLocator( gem, "application/x-rubygems" );
                    ((StorageFileItem) item).setContentLocator( locator );
                
                    rubyRepository.getRubygemsFacade().addGem( this, (StorageFileItem) item );
                }
                
                return;
            }
        }
        super.storeItem( repository, item );
    }
    
    @Override
    public void shredItem(Repository repository, ResourceStoreRequest request)
            throws ItemNotFoundException, UnsupportedStorageOperationException,
            LocalStorageException
    {
        RubyRepository rubyRepository = repository.adaptToFacet( RubyRepository.class );
        if ( rubyRepository != null ) 
        {
            if ( GemFile.isGem( request.getRequestPath() ) )
            {
            
                StorageFileItem item = (StorageFileItem) retrieveItem( rubyRepository, fixPath( request ) );
                try
                {
                
                    // remove the gem from the index files
                    rubyRepository.getRubygemsFacade().removeGem( this, item );
                
                }
                catch (IOException e) {
                    throw new LocalStorageException( "gem-file can not be found.", e );
                }
  
            }
            else
            {
                throw new UnsupportedStorageOperationException( "only gem files can be deleted" );
            }
        }
  
        super.shredItem( repository, request );
    }

    @Override
    public void moveItem(Repository repository, ResourceStoreRequest from,
            ResourceStoreRequest to) throws ItemNotFoundException,
            UnsupportedStorageOperationException, LocalStorageException
    {
        RubyRepository rubyRepository = repository.adaptToFacet( RubyRepository.class );
        if ( rubyRepository != null ) 
        {
            throw new UnsupportedStorageOperationException( "TODO why not ?" );
        }
        super.moveItem( repository, from, to );
    }

    // RubyLocalRepositoryStorage
    
    @Override
    public StorageFileItem retrieveSpecsIndex(RubyRepository repository,
            SpecsIndexType type, boolean gzipped) throws LocalStorageException, ItemNotFoundException {
        String extension = gzipped ? ".gz" : "";
        ResourceStoreRequest request = new ResourceStoreRequest( type.filepath() + extension );
        return (StorageFileItem) super.retrieveItem( repository,  request );
    }

    @Override
    public void storeSpecsIndex(RubyRepository repository, SpecsIndexType type,
            InputStream content) throws LocalStorageException, UnsupportedStorageOperationException {
        DefaultStorageFileItem item = new DefaultStorageFileItem( repository, new ResourceStoreRequest( type.filename() ), 
                true, true,
                new PreparedContentLocator( content , "application/x-marshal-ruby" ) );
        storeItem( repository,  item );
    }

    public void storeSpecsIndeces( RubyGroupRepository rubyRepository, SpecsIndexType type, List<StorageItem> specsItems)
            throws UnsupportedStorageOperationException, LocalStorageException  {
        StorageFileItem localSpecsItem = null;
        try
        {
            localSpecsItem = retrieveSpecsIndex( rubyRepository, type, false );
        }
        catch ( ItemNotFoundException e )
        {
            // Ignored. there are situations like after creating such a repo
        }
        if ( localSpecsItem != null )
        {
            for ( Iterator<StorageItem> iter = specsItems.iterator(); iter.hasNext(); )
            {                
                if ( iter.next().getModified() > localSpecsItem.getModified() )
                {
                   // iter.remove();
                }
            }
        }
        if ( !specsItems.isEmpty() )
        {
            try
            {
                rubyRepository.getRubygemsFacade().mergeSpecsIndex( this, type, localSpecsItem, specsItems );
            }
            catch ( IOException e ) {
                throw new LocalStorageException( e );
            }
        }
    }
}
