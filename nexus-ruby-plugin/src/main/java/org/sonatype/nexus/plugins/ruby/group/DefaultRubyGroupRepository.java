package org.sonatype.nexus.plugins.ruby.group;

import static org.sonatype.nexus.proxy.ItemNotFoundException.reasonFor;

import java.io.File;
import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Named;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.slf4j.Logger;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.plugins.ruby.RubyContentClass;
import org.sonatype.nexus.plugins.ruby.RubyGroupRepository;
import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.plugins.ruby.fs.RubygemFile;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.AbstractGroupRepository;
import org.sonatype.nexus.proxy.repository.DefaultRepositoryKind;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryKind;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.utils.RepositoryStringUtils;
import org.sonatype.nexus.ruby.RubygemsFile;

@Named( DefaultRubyGroupRepository.ID )
public class DefaultRubyGroupRepository
    extends AbstractGroupRepository
    implements RubyGroupRepository, GroupRepository
{
    public static final String ID = "rubygems-group";

    private final ContentClass contentClass;

    private final GroupRubyRepositoryConfigurator configurator;
    
    private final RepositoryKind repositoryKind;
    
    private final GroupNexusLayout layout;
    
    @Inject
    public DefaultRubyGroupRepository( @Named( RubyContentClass.ID ) ContentClass contentClass,
                                       GroupRubyRepositoryConfigurator configurator,
                                       GroupNexusLayout layout )
             throws LocalStorageException, ItemNotFoundException{
        this.contentClass = contentClass;
        this.configurator = configurator;  
        this.layout = layout;
        this.repositoryKind = new DefaultRepositoryKind( RubyGroupRepository.class,
                                                         Arrays.asList( new Class<?>[] { RubyRepository.class } ) );
    }

    @Override
    protected Configurator<Repository, CRepositoryCoreConfiguration> getConfigurator()
    {
        return configurator;
    }

    @Override
    protected CRepositoryExternalConfigurationHolderFactory<?> getExternalConfigurationHolderFactory()
    {
        return new CRepositoryExternalConfigurationHolderFactory<DefaultRubyGroupRepositoryConfiguration>()
        {
            public DefaultRubyGroupRepositoryConfiguration createExternalConfigurationHolder( CRepository config )
            {
                return new DefaultRubyGroupRepositoryConfiguration( (Xpp3Dom) config.getExternalConfiguration() );
            }
        };
    }

    public ContentClass getRepositoryContentClass()
    {
        return contentClass;
    }

    public RepositoryKind getRepositoryKind()
    {
        return repositoryKind;
    }

    // ==

    @Override
    protected DefaultRubyGroupRepositoryConfiguration getExternalConfiguration( boolean forWrite )
    {
        return (DefaultRubyGroupRepositoryConfiguration) super.getExternalConfiguration( forWrite );
    }


    // ==

    @SuppressWarnings("deprecation")
    @Override
    public StorageItem retrieveItem( ResourceStoreRequest request )
            throws AccessDeniedException, ItemNotFoundException, IllegalOperationException,
                   org.sonatype.nexus.proxy.StorageException
    {
        RubygemsFile file = layout.fromResourceStoreRequest( this, request );
        request.setRequestPath( file.storagePath() );
        switch( file.type() )
        {
        case GEM_ARTIFACT:
            return layout.retrieveGem( this, request, file.isGemArtifactFile() );
        case POM:
            return layout.createPom( this, request, file.isPomFile() );
        case MAVEN_METADATA:
            return layout.createMavenMetadata( this, request, file.isMavenMetadataFile() );
        case MAVEN_METADATA_SNAPSHOT:
            return layout.createMavenMetadataSnapshot( this, request, file.isMavenMetadataSnapshotFile() );
        case BUNDLER_API:
            return layout.createBundlerAPIResponse( this, file.isBundlerApiFile() );
        case SPECS_INDEX:
            if ( file.isSpecIndexFile().isGzipped() )
            {
                layout.setup( this, file );
            }
            else
            {
                return layout.retrieveUnzippedSpecsIndex( this, file.isSpecIndexFile() );
            }
            return super.retrieveItem( request );
        case DEPENDENCY:
            StorageItem item = layout.setup( this, file  );
            if ( item != null )
            {
                return item;
            }
        default:
            return retrieveFirstItem( request );
        }
    }

    @SuppressWarnings( "deprecation" )
    private StorageItem retrieveFirstItem( ResourceStoreRequest request )
        throws org.sonatype.nexus.proxy.StorageException,
               AccessDeniedException, IllegalOperationException, ItemNotFoundException
    {
        for( Repository repo : getMemberRepositories() )
        {
            try
            {
                return repo.retrieveItem( request );
            }
            catch (ItemNotFoundException e)
            {
               // ignore
            }
        } 
        throw new ItemNotFoundException( reasonFor( request, this,
                                                    "Could not find content for path %s in local storage of repository %s", 
                                                    request.getRequestPath(),
                                                    RepositoryStringUtils.getHumanizedNameString( this ) ) );
    }
 
    @Override
    public StorageItem retrieveJavaGem( RubygemFile gem )
    {
        throw new RuntimeException( "BUG: not implemented for group repositories" );
    }

    @Override
    public StorageItem retrieveJavaGemspec( RubygemFile gem )
    {
        throw new RuntimeException( "BUG: not implemented for group repositories" );
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public void storeItem( StorageItem item )
         throws org.sonatype.nexus.proxy.StorageException,
                UnsupportedStorageOperationException, IllegalOperationException
    {
        super.storeItem( false, item );
    }
    
    @Override
    public File getApplicationTempDirectory()
    {
        return getApplicationConfiguration().getTemporaryDirectory();
    }

    @Override
    public Logger getLog()
    {
        try
        {
            return log;
        }
        catch( java.lang.NoSuchFieldError e )
        {
            try
            {
                return (Logger) getClass().getSuperclass().getDeclaredMethod( "getLogger" ).invoke( this );
            }
            catch ( Exception ee )
            {
                throw new RuntimeException( "should work", ee );
            }
        }
    }
}