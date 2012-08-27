package org.sonatype.nexus.plugins.ruby.fs;

import java.io.File;
import java.io.InputStream;

import javax.inject.Inject;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.mime.MimeSupport;
import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.LinkPersister;
import org.sonatype.nexus.proxy.item.PreparedContentLocator;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.HostedRepository;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
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
    
    private final RubygemsFacade groupingFacade = new GroupingRubygemsFacade();
    private final RubygemsFacade hostedFacade = new HostedRubygemsFacade();

    @Inject
    public RubyFSLocalRepositoryStorage(Wastebasket wastebasket,
            LinkPersister linkPersister, MimeSupport mimeSupport, FSPeer fsPeer)
    {
        super( wastebasket, linkPersister, mimeSupport, fsPeer );
    }
    
    private RubygemsFacade facade(RubyRepository repository)
    {
        if (repository instanceof GroupRepository )
        {
            return groupingFacade;
        }
        if ( repository instanceof ProxyRepository )
        {
            return null;
        }
        if ( repository instanceof HostedRepository )
        {
            return hostedFacade;
        }
        return null;
    }
    
    private ResourceStoreRequest fixPath(ResourceStoreRequest request)
    {
        // put the gems into subdirectory with first-letter of the gems name
        request.setRequestPath( GemFile.fixPath( request.getRequestPath() ) );
        return request;
    }

    @Override
    protected AbstractStorageItem retrieveItemFromFile(Repository repository,
            ResourceStoreRequest request, File target)
            throws ItemNotFoundException, LocalStorageException
    {
        RubyRepository rubyRepository = repository.adaptToFacet( RubyRepository.class );
        if ( rubyRepository != null ) 
        {
            if ( request.getRequestPath().equals( "/gems/" ) ) 
            {
                return new RubygemsStorageCollectionItem( repository, request, target );
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
    public AbstractStorageItem retrieveItem(Repository repository,
            ResourceStoreRequest request) throws ItemNotFoundException,
            LocalStorageException 
    {
        RubyRepository rubyRepository = repository.adaptToFacet( RubyRepository.class );
        if ( rubyRepository != null ) 
        {
            SpecsIndexType type = SpecsIndexType.fromFilename( request.getRequestPath() );
            if ( type != null )
            {
                return (AbstractStorageItem) facade( rubyRepository ).retrieveSpecsIndex( rubyRepository, this, type, 
                    request.getRequestPath().endsWith( ".gz" ) );
            }
        }
        return super.retrieveItem( repository, fixPath( request ) );
    }

    @Override
    public void storeItem(Repository repository, StorageItem item)
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

                // add it to the index files
                File gem = getFileFromBase(rubyRepository, item.getResourceStoreRequest());
                ((StorageFileItem) item).setContentLocator(new FileContentLocator(gem, "application/x-rubygems"));
                facade( rubyRepository ).addGem( rubyRepository, this, gem );
                
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
            
                // first remove the gem from the index files
                StorageItem item = retrieveItem( rubyRepository, fixPath( request ) );
                File gem = getFileFromBase(rubyRepository, item.getResourceStoreRequest());
                facade( rubyRepository ).removeGem( rubyRepository, this, gem );
  
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
}
