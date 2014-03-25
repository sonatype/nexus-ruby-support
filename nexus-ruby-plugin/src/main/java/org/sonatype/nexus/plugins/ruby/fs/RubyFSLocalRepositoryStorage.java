package org.sonatype.nexus.plugins.ruby.fs;

import java.io.InputStream;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.mime.MimeSupport;
import org.sonatype.nexus.plugins.ruby.RubyGroupRepository;
import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.LinkPersister;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.storage.local.fs.DefaultFSLocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.local.fs.FSPeer;
import org.sonatype.nexus.proxy.wastebasket.Wastebasket;
import org.sonatype.nexus.ruby.BundlerDependencies;
import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.SpecsIndexType;

@Singleton
@Named( "rubyfile" )
public class RubyFSLocalRepositoryStorage 
    extends DefaultFSLocalRepositoryStorage
{

    @Inject
    public RubyFSLocalRepositoryStorage( Wastebasket wastebasket,
            LinkPersister linkPersister, MimeSupport mimeSupport, FSPeer fsPeer )
    {
        super( wastebasket, linkPersister, mimeSupport, fsPeer );
    }
    
    @Override
    public void storeItem( Repository repository, StorageItem item )
            throws UnsupportedStorageOperationException, LocalStorageException
    {
//        log.error( "ruby FS -----------> " + item );
//        log.error( "ruby FS -----------> " + item.getPath() );
//        log.error( "ruby FS -----------> " + item.getResourceStoreRequest() );
        RubygemsFile file = (RubygemsFile) item.getResourceStoreRequest().getRequestContext().get( RubygemsFile.class.getName() );
        if ( file != null )
        {
            item.getResourceStoreRequest().setRequestPath( file.storagePath() ); 
            ((AbstractStorageItem) item).setPath( file.storagePath() );
        }
        super.storeItem( repository, item );
    }
    
////        item.getResourceStoreRequest().setRequestPath( file.storagePath() );
//        if ( "/api/v1/api_key".equals( item.getPath() ) )
//        {
//            throw new RuntimeException( "not implemented !");
//        }
//        
//        if ( "/api/v1/gems".equals( item.getPath() ) )
//        {            
//            File tmpDir = getFileFromBase( repository, new ResourceStoreRequest( NEXUS_TEMP_PREFIX ) );
//            File tmpFile = null;
//            try
//            {
//                tmpDir.mkdirs();
//                tmpFile = File.createTempFile( "gems-", ".gem", tmpDir );
//                IOUtil.copy( ((StorageFileItem)item).getInputStream(), new FileOutputStream( tmpFile ) );
//
//                FileContentLocator locator = new FileContentLocator( tmpFile, "application/octect" );
//                ((StorageFileItem)item).setContentLocator( locator );
//                RubygemFile file2 = ( (RubyRepository) repository ).getRubygemsFacade().addGem( (RubyLocalRepositoryStorage) this, (StorageFileItem) item );
//                super.moveItem( repository, 
//                                new ResourceStoreRequest( new ResourceStoreRequest( NEXUS_TEMP_PREFIX +
//                                                                                    RepositoryItemUid.PATH_SEPARATOR +
//                                                                                    tmpFile.getName() ) ), 
//                                new ResourceStoreRequest( file2.getPath() ) );
//                
//                item.getResourceStoreRequest().setRequestPath( file2.getPath() );
//                ((AbstractStorageItem) item).setPath( file2.getPath() );
//            } 
//            catch ( IOException e )
//            {
//                throw new LocalStorageException( "error creating temp gem file", e );
//            }
//            catch (ItemNotFoundException e)
//            {
//                throw new LocalStorageException( "error moving gem file into right place", e );
//            }
//            finally
//            {
//                if ( tmpFile != null )
//                {
//                    tmpFile.delete();
//                }
//            }
//            return;
//        }
}
