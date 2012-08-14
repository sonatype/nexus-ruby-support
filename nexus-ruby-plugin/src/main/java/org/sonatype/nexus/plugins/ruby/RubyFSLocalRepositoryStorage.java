package org.sonatype.nexus.plugins.ruby;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.mime.MimeSupport;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.DefaultStorageCollectionItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.LinkPersister;
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
    
    static class RubygemsStorageCollectionItem extends DefaultStorageCollectionItem
    {

        private File gemsBasedir;
        private Repository repository;

        public RubygemsStorageCollectionItem( Repository repository,
                ResourceStoreRequest request, File gemsBasedir )
        {
            super( repository, request, true, false );
            this.gemsBasedir = gemsBasedir; 
            this.repository = repository;
        }

        public Collection<StorageItem> list()
        {
            Collection<StorageItem> result = new ArrayList<StorageItem>();

            visit( gemsBasedir, result );
            
            correctPaths( result );

            return result;
        }
        
        void visit( File path, Collection<StorageItem> list )
        {
            if( path.exists() && path.isDirectory() )
            {
                for( File f: path.listFiles() ){
                    visit( f, list );
                }
            }
            else if( path.getName().endsWith( ".gem" ) )
            {
                String gemPath = "/gems/" + path.getName();
                DefaultStorageFileItem item = new DefaultStorageFileItem( repository, new ResourceStoreRequest( gemPath, true, false ), 
                        true, false, new FileContentLocator( path, "binary/octet-stream" ) );
                item.setLength( path.length() );
                item.setModified( path.lastModified() );
                list.add( item );
            }
        }        
    }

    private ResourceStoreRequest fixPath(ResourceStoreRequest request)
    {
        // put the gems into subdirectory with first-letter of the gems name
        if ( request.getRequestPath().matches("^.*/gems/[^/]+\\.gem$") )
        {
            request.setRequestPath( request.getRequestPath().replaceFirst("/gems/([^/])([^/]+)\\.gem$", 
                    "/gems/$1/$1$2.gem"));
        }
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
                target = new File(new File(target.getParentFile(), target.getName().substring(0, 1)), target.getName());
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
        if ( item.getPath().matches( "^.*/gems/[^/]+\\.gem$" ) )
        {
            ((AbstractStorageItem) item).setPath( item.getPath().replaceFirst( "/gems/([^/])([^/]+)\\.gem$", 
                    "/gems/$1/$1$2.gem" ) );
            item.getResourceStoreRequest().setRequestPath( item.getPath() );
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
