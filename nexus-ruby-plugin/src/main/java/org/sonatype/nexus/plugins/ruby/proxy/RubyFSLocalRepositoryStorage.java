package org.sonatype.nexus.plugins.ruby.proxy;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.mime.MimeSupport;
import org.sonatype.nexus.plugins.ruby.shadow.Maven2RubyGemShadowRepository;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.DefaultStorageCollectionItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.LinkPersister;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.local.fs.DefaultFSLocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.local.fs.FSPeer;
import org.sonatype.nexus.proxy.storage.local.fs.FileContentLocator;
import org.sonatype.nexus.proxy.wastebasket.Wastebasket;

@Component( role = LocalRepositoryStorage.class, hint = DefaultFSLocalRepositoryStorage.PROVIDER_STRING )
public class RubyFSLocalRepositoryStorage extends DefaultFSLocalRepositoryStorage implements LocalRepositoryStorage{

    @Inject
    public RubyFSLocalRepositoryStorage(Wastebasket wastebasket,
            LinkPersister linkPersister, MimeSupport mimeSupport, FSPeer fsPeer) {
        super(wastebasket, linkPersister, mimeSupport, fsPeer);
    }

    @Override
    public File getFileFromBase( Repository repository,
            ResourceStoreRequest request, File repoBase )
            throws LocalStorageException {
        File result = super.getFileFromBase( repository, request, repoBase );
        
        if ( repository instanceof Maven2RubyGemShadowRepository && result.getName().contains( "mvn:" ) ) {
            result = new File( result.getParentFile(), result.getName().replaceFirst( "mvn:", "" ).replaceFirst( ":.*", "/" ) + result.getName() );
        }
        return result;
    }
    
    static class RubygemsStorageCollectionItem extends DefaultStorageCollectionItem {

        private File gemsBasedir;
        private Repository repository;

        public RubygemsStorageCollectionItem( Repository repository,
                ResourceStoreRequest request, File gemsBasedir ) {
            super( repository, request, true, false );
            this.gemsBasedir = gemsBasedir; 
            this.repository = repository;
        }

        public Collection<StorageItem> list(){
            Collection<StorageItem> result = new ArrayList<StorageItem>();

            visit( gemsBasedir, result );
            
            correctPaths( result );

            return result;
        }
        
        void visit( File path, Collection<StorageItem> list ){
            if( path.exists() && path.isDirectory() ){
                for( File f: path.listFiles() ){
                    visit( f, list );
                }
            }
            else if( path.getName().endsWith( ".gem" ) ){
                String gemPath = "/gems/" + path.getName();
                DefaultStorageFileItem item = new DefaultStorageFileItem( repository, new ResourceStoreRequest( gemPath, true, false ), 
                        true, false, new FileContentLocator( path, "binary/octet-stream" ) );
                item.setLength( path.length() );
                item.setModified( path.lastModified() );
                list.add( item );
            }
        }        
    }

    @Override
    protected AbstractStorageItem retrieveItemFromFile(Repository repository,
            ResourceStoreRequest request, File target)
            throws ItemNotFoundException, LocalStorageException {
        AbstractStorageItem item;
        if ( repository instanceof Maven2RubyGemShadowRepository && request.getRequestPath().equals( "/gems/" ) ) {
            item = new RubygemsStorageCollectionItem( repository, request, target );
        }
        else {
            item =  super.retrieveItemFromFile( repository, request, target );
        }
        
        return item;
    }

}
