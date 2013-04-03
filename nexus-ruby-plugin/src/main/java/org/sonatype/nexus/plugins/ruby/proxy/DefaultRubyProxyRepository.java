package org.sonatype.nexus.plugins.ruby.proxy;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.plugins.ruby.RubyContentClass;
import org.sonatype.nexus.plugins.ruby.RubyProxyRepository;
import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.plugins.ruby.fs.RubyLocalRepositoryStorage;
import org.sonatype.nexus.plugins.ruby.fs.RubygemsFacade;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.RemoteAccessException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.PreparedContentLocator;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.AbstractProxyRepository;
import org.sonatype.nexus.proxy.repository.DefaultRepositoryKind;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryKind;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.ruby.BundlerDependencies;
import org.sonatype.nexus.ruby.RubygemsGateway;
import org.sonatype.nexus.ruby.SpecsIndexType;

@Component( role = Repository.class, hint = DefaultRubyProxyRepository.ID, instantiationStrategy = "per-lookup", description = "RubyGem Proxy" )
public class DefaultRubyProxyRepository
    extends AbstractProxyRepository
    implements RubyProxyRepository, Repository
{

    public static final String ID = "rubygems-proxy";

    @Requirement( role = ContentClass.class, hint = RubyContentClass.ID )
    private ContentClass contentClass;

    @Requirement( role = DefaultRubyProxyRepositoryConfigurator.class )
    private DefaultRubyProxyRepositoryConfigurator defaultRubyProxyRepositoryConfigurator;
    
    @Requirement
    private RubygemsGateway gateway;

    private RubygemsFacade facade;
    
    @Override
    public RubygemsFacade getRubygemsFacade()
    {
        return facade;
    }

    @Override
    public void doConfigure() throws ConfigurationException
    {
        super.doConfigure();
        this.facade = new ProxyRubygemsFacade( gateway, this );
    }

    /**
     * Repository kind.
     */
    private final RepositoryKind repositoryKind = new DefaultRepositoryKind( RubyProxyRepository.class,
        Arrays.asList( new Class<?>[] { RubyRepository.class } ) );

    @Override
    protected Configurator getConfigurator()
    {
        return defaultRubyProxyRepositoryConfigurator;
    }

    @Override
    protected CRepositoryExternalConfigurationHolderFactory<?> getExternalConfigurationHolderFactory()
    {
        return new CRepositoryExternalConfigurationHolderFactory<DefaultRubyProxyRepositoryConfiguration>()
        {
            public DefaultRubyProxyRepositoryConfiguration createExternalConfigurationHolder( CRepository config )
            {
                return new DefaultRubyProxyRepositoryConfiguration( (Xpp3Dom) config.getExternalConfiguration() );
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
    protected DefaultRubyProxyRepositoryConfiguration getExternalConfiguration( boolean forWrite )
    {
        return (DefaultRubyProxyRepositoryConfiguration) super.getExternalConfiguration( forWrite );
    }

    @Override
    protected boolean isOld( StorageItem item )
    {
        if ( item.getName().contains( "specs.4.8" ) )
        {
            if (item.getName().endsWith( ".gz" ) )
            {
                getLogger().debug( item + " needs remote update: " + isOld( getExternalConfiguration( false ).getMetadataMaxAge(), item ) );
                return isOld( getExternalConfiguration( false ).getMetadataMaxAge(), item );
            }
            else
            {
                // whenever there is retrieve call to unzipped version it will be preceeded by call to zipped file
                return false;
            }
        }
        else
        {
            return isOld( getExternalConfiguration( false ).getArtifactMaxAge(), item );
        }
    }

    public int getArtifactMaxAge()
    {
        return getExternalConfiguration( false ).getArtifactMaxAge();
    }

    public void setArtifactMaxAge( int maxAge )
    {
        getExternalConfiguration( true ).setArtifactMaxAge( maxAge );
    }

    public int getMetadataMaxAge()
    {
        return getExternalConfiguration( false ).getMetadataMaxAge();
    }

    public void setMetadataMaxAge( int metadataMaxAge )
    {
        getExternalConfiguration( true ).setMetadataMaxAge( metadataMaxAge );
    }

    @SuppressWarnings("deprecation")
    @Override
    protected AbstractStorageItem doRetrieveRemoteItem(
            ResourceStoreRequest request ) throws ItemNotFoundException,
            RemoteAccessException, org.sonatype.nexus.proxy.StorageException {
        if ( request.getRequestPath().startsWith( "/api/" ) )
        {
            throw new ItemNotFoundException( request );
        }
        else
        {
            ResourceStoreRequest req = new ResourceStoreRequest( request.getRequestPath()
                                                                    .replaceFirst( "/gems/[^/]/", "/gems/" )
                                                                    .replaceFirst( "/Marshal.4.8/[^/]/", "/Marshal.4.8/" )
                                                                    .replaceFirst( ".4.8$", ".4.8.gz" ) );

            AbstractStorageItem item = super.doRetrieveRemoteItem( req );
            item.setResourceStoreRequest( request );
            item.setPath( request.getRequestPath() );
            return item;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public StorageItem retrieveItem(ResourceStoreRequest request)
            throws AccessDeniedException, IllegalOperationException,
            ItemNotFoundException, RemoteAccessException, org.sonatype.nexus.proxy.StorageException
    {
        SpecsIndexType type = SpecsIndexType.fromFilename(request.getRequestPath());

        if ( type != null && !request.getRequestPath().endsWith( ".gz" ) )
        {
            // make sure we have the gzipped file in place
            super.retrieveItem( new ResourceStoreRequest( type.filepathGzipped() ) );
        }
        else if ( request.getRequestPath().equals( "/api/v1/dependencies" ) )
        {
            BundlerDependencies bundler = facade.bundlerDependencies();
            String[] gemnames = request.getRequestUrl().replaceFirst( ".*gems=", "" ).replaceAll(",,", ",").replace("\\s+", "").split(",");
            facade.prepareDependencies( bundler, gemnames );
            
            return ((RubyLocalRepositoryStorage) getLocalStorage()).createBundlerDownloadable( this, bundler );
        }
        else if ( request.getRequestPath().startsWith( "/api/v1/dependencies/" ) )
        {
            BundlerDependencies bundler = facade.bundlerDependencies();
            String gemname = request.getRequestPath().replaceFirst( "^.*/", "" );
            return facade.prepareDependencies( bundler, gemname )[0];
        }
        return super.retrieveItem( request );
    }

    @Override
    @SuppressWarnings("deprecation")
    public StorageFileItem retrieveGemspec( String name ) 
            throws AccessDeniedException, IllegalOperationException, org.sonatype.nexus.proxy.StorageException, 
                    ItemNotFoundException
    {
        return (StorageFileItem) retrieveItem(new ResourceStoreRequest( "quick/Marshal.4.8/" + name + ".gemspec.rz" ) );
    }
    
    @Override
    public StorageFileItem retrieveDependenciesItem( String gemname ) 
            throws LocalStorageException, ItemNotFoundException
    {
        ResourceStoreRequest request = dependenciesRequest( gemname );
        if ( getLocalStorage().containsItem( this, request ) )
        {
            return (StorageFileItem) getLocalStorage().retrieveItem( this, dependenciesRequest( gemname ) );
        }
        else
        {
            return null;
        }
    }

    @Override
    public void storeDependencies( String gemname, String json )
                throws LocalStorageException, UnsupportedStorageOperationException
    {
        StorageFileItem result = new DefaultStorageFileItem( this,
              dependenciesRequest( gemname ), true, true,
              new PreparedContentLocator(
                      new ByteArrayInputStream( json.getBytes( Charset.forName( "UTF-8" ) ) ),
                      "application/json" ) );

        getLocalStorage().storeItem( this, result );
    }
    
    private ResourceStoreRequest dependenciesRequest( String gemname )
    {
        return new ResourceStoreRequest( "api/v1/dependencies/" + gemname.charAt(0) + "/" + gemname );
    }
}
