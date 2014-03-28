package org.sonatype.nexus.plugins.ruby.fs;

import static org.sonatype.nexus.proxy.ItemNotFoundException.reasonFor;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.RemoteAccessException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.utils.RepositoryStringUtils;
import org.sonatype.nexus.ruby.RubygemsGateway;
import org.sonatype.nexus.ruby.SpecsIndexType;

@Singleton
public class DefaultRubygemsFacade implements RubygemsFacade {

    protected final RubygemsGateway gateway;
    
    @Inject
    public DefaultRubygemsFacade( RubygemsGateway gateway )
    {
        this.gateway = gateway;
    }

    @SuppressWarnings( "deprecation" )
    @Override
    public StorageItem retrieveJavaGem( RubyRepository repository, RubygemFile gem )
            throws AccessDeniedException, IllegalOperationException,
                   ItemNotFoundException, RemoteAccessException,
                   org.sonatype.nexus.proxy.StorageException
    {
        return repository.retrieveItem( new ResourceStoreRequest( retrieveGemname( repository, gem ).getPath() ) );
    }
    
    @SuppressWarnings( "deprecation" )
    @Override
    public StorageItem retrieveJavaGemspec( RubyRepository repository, RubygemFile gem )
            throws AccessDeniedException, IllegalOperationException,
                   ItemNotFoundException, RemoteAccessException,
                   org.sonatype.nexus.proxy.StorageException
    {
        return repository.retrieveItem( new ResourceStoreRequest( retrieveGemname( repository, gem ).getGemspecRz() ) );
    }

    @SuppressWarnings( "deprecation" )
    protected RubygemFile retrieveGemname( RubyRepository repository,
                                           RubygemFile gem )
            throws ItemNotFoundException, IllegalOperationException,
            org.sonatype.nexus.proxy.StorageException, AccessDeniedException
    {
        String path = ( gem.isPreleasedGem() ? SpecsIndexType.PRERELEASE : SpecsIndexType.RELEASE ).filepath();
        StorageFileItem specs = (StorageFileItem) repository.retrieveItem( new ResourceStoreRequest( path ) );
        String gemname;
        InputStream in = null;
        try
        {
            in = specs.getContentLocator().getContent();
            gemname = gateway.gemnameWithPlatform( gem.getGemname(),
                                                   gem.getGemVersion(),
                                                   in,
                                                   specs.getModified() );
        }
        catch ( IOException e )
        {
            throw new ItemNotFoundException( reasonFor( new ResourceStoreRequest( gem.getPath() ),
                                                        repository,
                                                        "Path %s not found in repository %s",
                                                        RepositoryStringUtils.getHumanizedNameString( repository ) ),
                                                        e );
        }
        finally
        {
            IOUtil.close( in );
        }
        if ( gemname == null ){
            throw new ItemNotFoundException( reasonFor( new ResourceStoreRequest( gem.getPath() ),
                                                        repository,
                                                        "Path %s not found in repository %s",
                                                        gem.getPath(),
                                                        RepositoryStringUtils.getHumanizedNameString( repository ) ) );
        }
        RubygemFile newGem = RubygemFile.newGem( gemname + ".gem" );
        return newGem;
    }
} 
