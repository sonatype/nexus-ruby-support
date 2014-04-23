package org.sonatype.nexus.plugins.ruby.hosted;

import static org.sonatype.nexus.proxy.ItemNotFoundException.reasonFor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.slf4j.Logger;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.plugins.ruby.RubyContentClass;
import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.plugins.ruby.fs.DefaultRubygemsFacade;
import org.sonatype.nexus.plugins.ruby.fs.RubygemFile;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.RemoteAccessException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.AbstractRepository;
import org.sonatype.nexus.proxy.repository.DefaultRepositoryKind;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryKind;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.utils.RepositoryStringUtils;
import org.sonatype.nexus.ruby.FileType;
import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.RubygemsGateway;
import org.sonatype.nexus.ruby.SpecsIndexFile;

@Named( DefaultHostedRubyRepository.ID )
public class DefaultHostedRubyRepository
    extends AbstractRepository
    implements HostedRubyRepository, Repository
{
    public static final String ID = "rubygems-hosted";

    private final ContentClass contentClass;

    private final HostedRubyRepositoryConfigurator configurator;

    private final RubygemsGateway gateway;
        
    private final DefaultRubygemsFacade facade;

    private final RepositoryKind repositoryKind;

    private final HostedNexusLayout layout;
    
    @Inject
    public DefaultHostedRubyRepository( @Named( RubyContentClass.ID ) ContentClass contentClass,
                                        HostedRubyRepositoryConfigurator configurator,
                                        HostedNexusLayout layout,
                                        RubygemsGateway gateway,
                                        DefaultRubygemsFacade facade )
             throws LocalStorageException, ItemNotFoundException{
        this.contentClass = contentClass;
        this.configurator = configurator;
        this.gateway = gateway;
        this.layout = layout;
        this.facade = facade;
        this.repositoryKind = new DefaultRepositoryKind( HostedRubyRepository.class,
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
        return new CRepositoryExternalConfigurationHolderFactory<DefaultHostedRubyRepositoryConfiguration>()
        {
            public DefaultHostedRubyRepositoryConfiguration createExternalConfigurationHolder( CRepository config )
            {
                return new DefaultHostedRubyRepositoryConfiguration( (Xpp3Dom) config.getExternalConfiguration() );
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
    protected DefaultHostedRubyRepositoryConfiguration getExternalConfiguration( boolean forWrite )
    {
        return (DefaultHostedRubyRepositoryConfiguration) super.getExternalConfiguration( forWrite );
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public void storeItem( StorageItem item )
            throws org.sonatype.nexus.proxy.StorageException,
                UnsupportedStorageOperationException, IllegalOperationException
    {
        super.storeItem( false, item );
    }
    
    @SuppressWarnings( "deprecation" )
    public void storeItem(ResourceStoreRequest request, InputStream is, Map<String, String> userAttributes)
            throws UnsupportedStorageOperationException, IllegalOperationException, 
                   org.sonatype.nexus.proxy.StorageException, AccessDeniedException
    {
        RubygemsFile file = layout.fromResourceStoreRequestOrNull( request );
        if ( file != null && !request.getRequestPath().equals( file.storagePath() ) )
        {
            request.setRequestPath( file.storagePath() );
        }
        super.storeItem( request, is, userAttributes );
    }
    
    @SuppressWarnings( "deprecation" )
    public void storeItem( boolean fromTask, StorageItem item )
       throws UnsupportedStorageOperationException, 
              org.sonatype.nexus.proxy.StorageException, 
              IllegalOperationException
    {
        RubygemsFile file = layout.fromStorageItem( item );
        switch( file.type() )
        {
        case GEM:
            try
            {       
                super.storeItem( fromTask, item );
                layout.storeGem( this, file.isGemFile() );
            }
            catch (ItemNotFoundException | AccessDeniedException e)
            {
                new org.sonatype.nexus.proxy.StorageException( e );
            }
            break;
        case API_V1:
            if ( "gems".equals( file.name() ) )
            {
                try
                {
                    layout.storeGem( this, ((StorageFileItem) item ).getInputStream() );
                }
                catch (IOException e)
                {
                    new org.sonatype.nexus.proxy.StorageException( e );
                }
            } 
            break;
        default:
            throw new UnsupportedStorageOperationException( "only gem-files can be stored" );
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void deleteItem( ResourceStoreRequest request )
            throws org.sonatype.nexus.proxy.StorageException, 
                UnsupportedStorageOperationException, IllegalOperationException,
                ItemNotFoundException, AccessDeniedException
    {
        RubygemsFile file = layout.fromResourceStoreRequest( this, request );
        if( file.type() == FileType.GEM )
        {
            layout.deleteGem( this, file.isGemFile() );
            super.deleteItem( layout.toResourceStoreRequest( file ) );
            super.deleteItem( layout.toResourceStoreRequest( file.isGemFile().gemspec() ) );
        }
        else
        {
            throw new UnsupportedStorageOperationException( "only gem-files can be deleted" );
        }
    }

    @Override
    public void moveItem( ResourceStoreRequest from, ResourceStoreRequest to)
            throws UnsupportedStorageOperationException
    {
        throw new UnsupportedStorageOperationException( "not supported" );
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public StorageItem retrieveItem( boolean fromTask, ResourceStoreRequest request )
            throws //AccessDeniedException,
                   IllegalOperationException,
                   ItemNotFoundException, RemoteAccessException, 
                   org.sonatype.nexus.proxy.StorageException
    {
        RubygemsFile file = layout.fromResourceStoreRequest( this, request );
        request.setRequestPath( file.storagePath() );
        try
        {
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
            case API_V1:
                if ( "api_key".equals( file.isApiV1File().name() ) )
                {
                    // TODO not sure how
                }
                throw new ItemNotFoundException( reasonFor( request, this,
                                                            "Can not serve path %s for repository %s", 
                                                            request.getRequestPath(),
                                                            RepositoryStringUtils.getHumanizedNameString( this ) ) );
            case DEPENDENCY:
                try
                {
                    return super.retrieveItem( fromTask, request );
                }
                catch( ItemNotFoundException e )
                {
                    layout.createDependency( this, file.isDependencyFile() );
                    return super.retrieveItem( fromTask, request );                
                }
            case GEMSPEC:
                try
                {
                    return super.retrieveItem( fromTask, request );
                }
                catch( ItemNotFoundException e )
                {
                    layout.createGemspec( this, file.isGemspecFile() );
                    return super.retrieveItem( fromTask, request );                
                }
            case SPECS_INDEX:
                SpecsIndexFile specs = file.isSpecIndexFile();
                if ( ! specs.isGzipped() )
                {
                    return layout.retrieveUnzippedSpecsIndex( this, specs );
                }
                else
                {
                    try
                    {
                        return super.retrieveItem( fromTask, request );
                    }
                    catch (ItemNotFoundException e)
                    {
                        layout.createEmptySpecs( this, specs.specsType() );
                    }
                }
            default:
                return super.retrieveItem( fromTask, request );
            }
        }
        catch( AccessDeniedException e )
        {
            throw new org.sonatype.nexus.proxy.StorageException( e );
        }
    }

    @Override
    public void recreateMetadata() throws LocalStorageException, ItemNotFoundException
    {
        String directory = getBaseDirectory();
        if (getLog().isDebugEnabled()){
            getLog().debug( "recreate rubygems metadata in " + directory );
        }
        gateway.recreateRubygemsIndex( directory );
        gateway.purgeBrokenDepencencyFiles( directory );
    }

    protected String getBaseDirectory() throws ItemNotFoundException,
            LocalStorageException
    {
        return this.getLocalUrl().replace( "file:", "" );
    }

    @SuppressWarnings( "deprecation" )
    @Override
    public StorageItem retrieveJavaGem( RubygemFile gem )
            throws AccessDeniedException, IllegalOperationException,
                   ItemNotFoundException, RemoteAccessException,
                   org.sonatype.nexus.proxy.StorageException
    {
        return facade.retrieveJavaGem( this, gem );
    }
 
    @SuppressWarnings( "deprecation" )
    @Override
    public StorageItem retrieveJavaGemspec( RubygemFile gem )
            throws AccessDeniedException, IllegalOperationException,
                   ItemNotFoundException, RemoteAccessException,
                   org.sonatype.nexus.proxy.StorageException
    {
        return facade.retrieveJavaGemspec( this, gem );
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
