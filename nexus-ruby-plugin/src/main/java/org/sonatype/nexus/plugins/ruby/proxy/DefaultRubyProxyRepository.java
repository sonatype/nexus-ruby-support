package org.sonatype.nexus.plugins.ruby.proxy;

import java.util.Arrays;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.mockito.internal.matchers.EndsWith;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.plugins.ruby.JRubyRubyGateway;
import org.sonatype.nexus.plugins.ruby.RubyContentClass;
import org.sonatype.nexus.plugins.ruby.RubyGateway;
import org.sonatype.nexus.plugins.ruby.RubyProxyRepository;
import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.plugins.ruby.fs.RubygemsFacade;
import org.sonatype.nexus.plugins.ruby.fs.SpecsIndexType;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.RemoteAccessException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.AbstractProxyRepository;
import org.sonatype.nexus.proxy.repository.DefaultRepositoryKind;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryKind;

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

    private final RubyGateway gateway = new JRubyRubyGateway();

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
        if ( item.getName().contains( "specs" ) )
        {
            // time in minutes
            // TODO get the MaxAge for metadate from config
            return isOld( 50, item );
        }
        return super.isOld( item );
    }

    @Override
    protected AbstractStorageItem doRetrieveRemoteItem(
            ResourceStoreRequest request ) throws ItemNotFoundException,
            RemoteAccessException, StorageException {
        if ( request.getRequestPath().startsWith( "/api/v1/" ) )
        {
            throw new ItemNotFoundException( request );
        }
        else
        {
            ResourceStoreRequest req = new ResourceStoreRequest( request );
            req.setRequestPath( request.getRequestPath().replaceFirst( "^/gems/[^/]/", "/gems/" ) );
            AbstractStorageItem item = super.doRetrieveRemoteItem( req );
            item.setResourceStoreRequest(request);
            item.setPath(request.getRequestPath());
            return item;
        }
    }

    @Override
    public StorageItem retrieveItem(
            ResourceStoreRequest request ) throws AccessDeniedException, IllegalOperationException, 
            ItemNotFoundException, RemoteAccessException , StorageException {
        SpecsIndexType type = SpecsIndexType.fromFilename( request.getRequestPath() );

        if ( type != null && !request.getRequestPath().endsWith( ".gz" ) )
        {
            // make sure the gzipped version of the file is downloaded and cached
            ResourceStoreRequest req = new ResourceStoreRequest( request );
            req.setRequestPath( request.getRequestPath() + ".gz" );
            super.retrieveItem( new ResourceStoreRequest( request.getRequestPath() + ".gz" ) );
        }   

        return super.retrieveItem( request );
    }
    
    @Override
    public void synchronizeWithRemoteRepository()
    {
        throw new RuntimeException("TODO");
    }
}
