package org.sonatype.nexus.plugins.ruby.shadow;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.sonatype.nexus.plugins.ruby.RubyGateway;
import org.sonatype.nexus.plugins.ruby.RubyRepositoryHelper;
import org.sonatype.nexus.plugins.ruby.RubyShadowRepository;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.item.ContentGenerator;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.storage.local.fs.FileContentLocator;
import org.sonatype.nexus.ruby.MavenArtifact;

@Component( role = ContentGenerator.class, hint = Maven2RubyGemShadowContentGenerator.ID )
public class Maven2RubyGemShadowContentGenerator
    implements ContentGenerator
{
    public static final String ID = Maven2RubyGemShadowRepository.ID;

    @Requirement
    private Logger logger;

    protected Logger getLogger()
    {
        return logger;
    }

    @Requirement
    private RubyRepositoryHelper rubyRepositoryHelper;

    @Requirement
    private RubyGateway rubyGateway;

    /**
     * What we do here is pretty hacky: on-demand GEM creation. If this ContentGenerator is invoked, it means that an
     * item from Shadow repository is request (see shadow repository about how these stub items are laid down). The
     * point is, that we recreate the same item that is requested by making real Gem for it, and removing the
     * ContentGenerator marking. Hence, for one item this ContentGenerator will hit only once, and not anymore, since
     * the real Gem will be served as usual.
     */
    public ContentLocator generateContent( Repository repository, String path, StorageFileItem item )
        throws IllegalOperationException, ItemNotFoundException, StorageException
    {
        try
        {
            // get the master repository
            MavenRepository masterRepository = ( (RubyShadowRepository) repository ).getMasterRepository();

            // get the original JAR we want to convert
            StorageFileItem originalItem = retrieveOriginalFileItem( masterRepository, item );

            // generate one on the fly
            MavenArtifact mart = rubyRepositoryHelper.getMavenArtifactForItem( masterRepository, originalItem );

            // set the proper path of the generated Gem file (which is actually the request that initiated this
            // processing)
            File target = File.createTempFile( "nexus-gem", ".gem.tmp" );

            // do conversion
            rubyGateway.createGemFromArtifact( mart, target );

            // swap in the content locator
            FileContentLocator gemFileContentLocator = new FileContentLocator( target, "binary/octet-stream" );

            DefaultStorageFileItem gemItem =
                new DefaultStorageFileItem( repository, item.getResourceStoreRequest(), true, true,
                    gemFileContentLocator );
            // move over all attributes
            gemItem.getAttributes().putAll( item.getAttributes() );
            // except the content generator one, since we generated it
            gemItem.getAttributes().remove( ContentGenerator.CONTENT_GENERATOR_ID );

            // replace it but with locking!
            item.getRepositoryItemUid().lock( Action.create );
            try
            {
                repository.deleteItem( true, item.getResourceStoreRequest() );
                repository.storeItem( true, gemItem );
            }
            finally
            {
                item.getRepositoryItemUid().unlock();
                // cleanup
                target.delete();
            }

            StorageFileItem newGemItem =
                (StorageFileItem) repository.retrieveItem( true, new ResourceStoreRequest( item ) );
            
            item.setLength( newGemItem.getLength() );

            return newGemItem.getContentLocator();
        }
        catch ( UnsupportedStorageOperationException e )
        {
            throw new StorageException( "Could not create GEM!", e );
        }
        catch ( StorageException e )
        {
            throw new StorageException( "Could not create GEM!", e );
        }
        catch ( IOException e )
        {
            throw new StorageException( "Could not create GEM!", e );
        }
    }

    // ==

    protected StorageFileItem retrieveOriginalFileItem( Repository repository, StorageFileItem item )
        throws StorageException, IllegalOperationException, ItemNotFoundException
    {
        String originalItemPath = item.getAttributes().get( Maven2RubyGemShadowRepository.ORIGINAL_ITEM_PATH );

        ResourceStoreRequest originalRequest = new ResourceStoreRequest( item );

        originalRequest.setRequestPath( originalItemPath );

        StorageFileItem originalItem = (StorageFileItem) repository.retrieveItem( true, originalRequest );

        return originalItem;
    }
}
