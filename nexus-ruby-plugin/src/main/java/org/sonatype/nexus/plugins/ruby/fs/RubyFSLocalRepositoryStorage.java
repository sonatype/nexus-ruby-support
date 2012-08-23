package org.sonatype.nexus.plugins.ruby.fs;

import java.io.File;

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
public class RubyFSLocalRepositoryStorage extends DefaultFSLocalRepositoryStorage implements LocalRepositoryStorage
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
    protected AbstractStorageItem retrieveItemFromFile(Repository repository,
            ResourceStoreRequest request, File target)
            throws ItemNotFoundException, LocalStorageException
    {
        if ( repository instanceof RubyRepository ) 
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
            else
            { 
                SpecsIndexType type = SpecsIndexType.fromFilename( request.getRequestPath() );
                if ( type != null)
                {
                    RubyRepository rubyRepository = (RubyRepository) repository;
                    RubygemsIndex index = new RubygemsIndex(null);
                    try {
                        if ( request.getRequestPath().endsWith( ".gz" ) )
                        {
                            
                            index.retrieveSpecsGz( rubyRepository, type );
                        
                        }
                        else
                        {
                        
                            index.retrieveSpecs( rubyRepository, type );
                        
                        }
                    }
                    catch ( RuntimeException e )
                    {
                        throw e;
                    }
                    catch ( Exception e )
                    {
                        throw new ItemNotFoundException( request, e );
                    }
                }
            }
        }
        return super.retrieveItemFromFile( repository, request, target );
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
        return super.retrieveItem( repository, fixPath( request ) );
    }

    @Override
    public void storeItem(Repository repository, StorageItem item)
            throws UnsupportedStorageOperationException, LocalStorageException
    {
        if ( repository instanceof RubyRepository )
        {
            if ( GemFile.isGem( item.getPath() ) )
            {
                ((AbstractStorageItem) item).setPath( GemFile.fixPath( item.getPath() ) );
                item.getResourceStoreRequest().setRequestPath( item.getPath() );
                RubygemsIndex index = new RubygemsIndex((RubyRepository) repository);
                try
                {

                    super.storeItem( repository, item );
                    index.add( (StorageFileItem) item );
  
                } 
                catch (RuntimeException e)
                {
                    throw e;
                }
                catch (Exception e)
                {
                    throw new LocalStorageException( "error updating rubygems index", e );
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
        super.shredItem( repository, fixPath( request ) );
    }

    @Override
    public void moveItem(Repository repository, ResourceStoreRequest from,
            ResourceStoreRequest to) throws ItemNotFoundException,
            UnsupportedStorageOperationException, LocalStorageException
    {
        super.moveItem( repository, fixPath(from), fixPath(to) );
    }
}
