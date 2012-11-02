package org.sonatype.nexus.plugins.ruby.shadow;

import java.io.File;

import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.FileContentLocator;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StorageLinkItem;
import org.sonatype.nexus.proxy.maven.ArtifactStoreHelper;
import org.sonatype.nexus.proxy.maven.ArtifactStoreRequest;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.gav.Gav;

final class GemArtifactStoreHelper extends ArtifactStoreHelper {
    
    GemArtifactStoreHelper(MavenRepository repo) {
        super(repo);
    }
    
    public StorageFileItem retrieveArtifact( ArtifactStoreRequest gavRequest )
        throws IllegalOperationException, ItemNotFoundException, StorageException, AccessDeniedException
        {
            checkRequest( gavRequest );

            Gav gav = resolveArtifact( gavRequest );

            gavRequest.setRequestPath( getMavenRepository().getGavCalculator().gavToPath( gav ) );

            StorageItem item = getMavenRepository().retrieveItem( gavRequest );

            if ( StorageLinkItem.class.isAssignableFrom( item.getClass() ) )
            {
                File file = new File( ( (StorageLinkItem) item ).getTarget().getPath() );
                return new DefaultStorageFileItem( 
                            getMavenRepository(), gavRequest, true, false, 
                            new FileContentLocator( file, "binary/octet-stream" )
                        );
            }
            else
            {
                throw new LocalStorageException( "The Artifact retrieval returned non-file, path:"
                    + item.getRepositoryItemUid().toString() );
            }
        }
}