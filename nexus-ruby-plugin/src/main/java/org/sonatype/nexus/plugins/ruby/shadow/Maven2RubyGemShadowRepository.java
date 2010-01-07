package org.sonatype.nexus.plugins.ruby.shadow;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.plugins.ruby.RubyContentClass;
import org.sonatype.nexus.plugins.ruby.RubyGateway;
import org.sonatype.nexus.plugins.ruby.RubyIndexer;
import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.plugins.ruby.RubyRepositoryHelper;
import org.sonatype.nexus.plugins.ruby.RubyShadowRepository;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.ContentGenerator;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StorageLinkItem;
import org.sonatype.nexus.proxy.item.StringContentLocator;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.AbstractShadowRepository;
import org.sonatype.nexus.proxy.repository.DefaultRepositoryKind;
import org.sonatype.nexus.proxy.repository.IncompatibleMasterRepositoryException;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryKind;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.storage.local.fs.DefaultFSLocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.local.fs.FileContentLocator;
import org.sonatype.nexus.ruby.MavenArtifact;

@Component( role = ShadowRepository.class, hint = Maven2RubyGemShadowRepository.ID )
public class Maven2RubyGemShadowRepository
    extends AbstractShadowRepository
    implements RubyShadowRepository
{
    public static final String ID = "maven2-gem";

    public static final String ORIGINAL_ITEM_PATH = "item.originalItemPath";

    @Requirement( role = ContentClass.class, hint = RubyContentClass.ID )
    private ContentClass contentClass;

    @Requirement( role = ContentClass.class, hint = Maven2ContentClass.ID )
    private ContentClass masterContentClass;

    @Requirement( role = Maven2RubyGemShadowRepositoryConfigurator.class )
    private Maven2RubyGemShadowRepositoryConfigurator maven2RubyGemShadowRepositoryConfigurator;

    @Requirement
    private RubyRepositoryHelper rubyRepositoryHelper;

    @Requirement
    private RubyIndexer rubyIndexer;

    @Requirement
    private RubyGateway rubyGateway;

    /**
     * Repository kind.
     */
    private final RepositoryKind repositoryKind =
        new DefaultRepositoryKind( RubyShadowRepository.class, Arrays.asList( new Class<?>[] { RubyRepository.class } ) );

    @Override
    protected Configurator getConfigurator()
    {
        return maven2RubyGemShadowRepositoryConfigurator;
    }

    @Override
    protected CRepositoryExternalConfigurationHolderFactory<?> getExternalConfigurationHolderFactory()
    {
        return new CRepositoryExternalConfigurationHolderFactory<Maven2RubyGemShadowRepositoryConfiguration>()
        {
            public Maven2RubyGemShadowRepositoryConfiguration createExternalConfigurationHolder( CRepository config )
            {
                return new Maven2RubyGemShadowRepositoryConfiguration( (Xpp3Dom) config.getExternalConfiguration() );
            }
        };
    }

    public RepositoryKind getRepositoryKind()
    {
        return repositoryKind;
    }

    public ContentClass getRepositoryContentClass()
    {
        return contentClass;
    }

    public ContentClass getMasterRepositoryContentClass()
    {
        return masterContentClass;
    }

    // == RubyShadowRepository

    @Override
    public MavenRepository getMasterRepository()
    {
        return (MavenRepository) super.getMasterRepository();
    }

    @Override
    public void setMasterRepository( Repository masterRepository )
        throws IncompatibleMasterRepositoryException
    {
        if ( masterRepository instanceof MavenRepository )
        {
            super.setMasterRepository( masterRepository );
        }
        else
        {
            throw new IncompatibleMasterRepositoryException( this, masterRepository.getId() );
        }
    }

    public boolean isLazyGemMaterialization()
    {
        return getExternalConfiguration( false ).isLazyGemMaterialization();
    }

    public void setLazyGemMaterialization( boolean val )
    {
        getExternalConfiguration( true ).setLazyGemMaterialization( val );
    }

    // ==

    @Override
    protected StorageLinkItem createLink( StorageItem item )
        throws UnsupportedStorageOperationException, IllegalOperationException, StorageException
    {
        // operate of files only
        if ( !( item instanceof StorageFileItem ) )
        {
            return null;
        }

        try
        {
            MavenArtifact mart = null;

            try
            {
                mart = rubyRepositoryHelper.getMavenArtifactForItem( getMasterRepository(), (StorageFileItem) item );
            }
            catch ( StorageException e )
            {
                // neglect it, will be skipped, look below
            }

            if ( mart == null )
            {
                getLogger().debug( "Skipping artifact " + item.getPath() + " in repository " + getId() );

                return null;
            }

            String gemName = rubyGateway.getGemFileName( mart.getPom() );

            getLogger().debug(
                "Creating " + ( isLazyGemMaterialization() ? "lazily " : "" ) + " Gem " + gemName + " in repository "
                    + getId() );

            if ( isLazyGemMaterialization() )
            {
                // 1st, we create the stub file, which is just the gemspec file in Yaml
                DefaultStorageFileItem gemStub =
                    new DefaultStorageFileItem( this, new ResourceStoreRequest( "/gems/" + gemName, true ), true,
                        false, new StringContentLocator( "STUB" ) );

                gemStub.getAttributes().put( ContentGenerator.CONTENT_GENERATOR_ID,
                    Maven2RubyGemShadowContentGenerator.ID );

                gemStub.getAttributes().put( ORIGINAL_ITEM_PATH, item.getPath() );

                storeItem( true, gemStub );

                // 2nd, we create the raw gemspec file for indexer
                // THIS IS DIRTY!
                File gemspecFile =
                    ( (DefaultFSLocalRepositoryStorage) getLocalStorage() ).getFileFromBase( this,
                        new ResourceStoreRequest( "/gems/" + gemName + ".gemspec" ) );

                rubyGateway.createAndWriteGemspec( mart.getPom(), gemspecFile );
            }
            else
            {
                File target = File.createTempFile( "nexus-gem", ".gem.tmp" );

                rubyGateway.createGemFromArtifact( mart, target );

                DefaultStorageFileItem gemItem =
                    new DefaultStorageFileItem( this, new ResourceStoreRequest( "/gems/" + gemName, true ), true,
                        false, new FileContentLocator( target, "binary/octet-stream" ) );

                gemItem.getAttributes().put( ORIGINAL_ITEM_PATH, item.getPath() );

                storeItem( true, gemItem );
            }
        }
        catch ( IOException e )
        {
            // should not happen
            // TODO: fix this exception getGemSpecificationIO().write()
        }

        rubyIndexer.reindexRepository( this );

        // nothing to return
        return null;
    }

    @Override
    protected void deleteLink( StorageItem item )
        throws UnsupportedStorageOperationException, IllegalOperationException, ItemNotFoundException, StorageException
    {
        MavenArtifact mart =
            rubyRepositoryHelper.getMavenArtifactForItem( getMasterRepository(), (StorageFileItem) item );

        String gemName = rubyGateway.getGemFileName( mart.getPom() );

        deleteItem( true, new ResourceStoreRequest( "/gems/" + gemName ) );

        deleteItem( true, new ResourceStoreRequest( "/gems/" + gemName + ".gemspec" ) );

        rubyIndexer.reindexRepository( this );
    }

    // ==

    @Override
    protected Maven2RubyGemShadowRepositoryConfiguration getExternalConfiguration( boolean forWrite )
    {
        return (Maven2RubyGemShadowRepositoryConfiguration) super.getExternalConfiguration( forWrite );
    }

    // ==

    @Override
    public void synchronizeWithMaster()
    {
        getLogger().info(
            "Syncing GEM shadow " + getId() + " with Maven2 master repository " + getMasterRepository().getId() );

        ResourceStoreRequest request = new ResourceStoreRequest( RepositoryItemUid.PATH_ROOT, true );

        expireCaches( request );

        try
        {
            File masterBasedir = rubyRepositoryHelper.getMavenRepositoryBasedir( getMasterRepository() );

            DirectoryScanner scanner = new DirectoryScanner();

            scanner.setBasedir( masterBasedir );
            scanner.addDefaultExcludes();
            scanner.setIncludes( new String[] { "**/*.pom" } );

            scanner.scan();

            for ( String pomPath : scanner.getIncludedFiles() )
            {
                request.pushRequestPath( pomPath );

                try
                {
                    StorageItem item = getMasterRepository().retrieveItem( request );

                    if ( item instanceof StorageFileItem )
                    {
                        synchronizeLink( item );
                    }
                }
                catch ( Exception e )
                {
                    // neglect any exception but log it
                    getLogger().warn(
                        "Got exception with path \"" + pomPath + "\" while syncing GEM shadow " + getId()
                            + " with Maven2 master repository " + getMasterRepository().getId(), e );
                }

                request.popRequestPath();
            }
        }
        catch ( StorageException e )
        {
            getLogger().warn(
                "Got storage exception while syncing GEM shadow " + getId() + " with Maven2 master repository "
                    + getMasterRepository().getId(), e );
        }
    }
}
