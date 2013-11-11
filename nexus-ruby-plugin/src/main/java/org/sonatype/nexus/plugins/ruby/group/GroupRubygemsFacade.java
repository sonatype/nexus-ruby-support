package org.sonatype.nexus.plugins.ruby.group;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.plugins.ruby.RubyGroupRepository;
import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.plugins.ruby.fs.AbstractRubygemsFacade;
import org.sonatype.nexus.plugins.ruby.fs.RubyLocalRepositoryStorage;
import org.sonatype.nexus.plugins.ruby.fs.RubygemsFacade;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.ruby.BundlerDependencies;
import org.sonatype.nexus.ruby.RubygemsGateway;
import org.sonatype.nexus.ruby.SpecsIndexType;

public class GroupRubygemsFacade extends AbstractRubygemsFacade {

    public GroupRubygemsFacade( RubygemsGateway gateway, RubyRepository repository )
    {
        super( gateway, repository );
    }

    @Override
    public void mergeSpecsIndex( RubyLocalRepositoryStorage storage, SpecsIndexType type,
            StorageItem localItem, List<StorageItem> specsItems )
            throws UnsupportedStorageOperationException, LocalStorageException, IOException {
        List<InputStream> streams = new LinkedList<InputStream>();
        for( StorageItem item: specsItems )
        {
            streams.add( toGZIPInputStream( (StorageFileItem) item ) );
        }
        InputStream is = localItem == null ? null : toGZIPInputStream( (StorageFileItem) localItem );
        storeSpecsIndex( repository, 
                         storage, 
                         type,
                         gateway.mergeSpecs( is, streams, type == SpecsIndexType.LATEST ) );
    }   
  
    @Override
    public BundlerDependencies bundlerDependencies() 
    {
        return gateway.newBundlerDependencies();
    }
    
    @Override
    protected StorageFileItem dependencyMap( RubyLocalRepositoryStorage storage, 
                                             String gemname )
            throws ItemNotFoundException, AccessDeniedException,
            IllegalOperationException, StorageException {

        String json = mergeDependencies( bundlerDependencies(), gemname );
        
        return storage.createTempStorageFile( this.repository, 
                                              new ByteArrayInputStream( json.getBytes() ),
                                              "application/json" );
    }

    private String mergeDependencies( BundlerDependencies bundlerDependencies, String gemname ) 
            throws AccessDeniedException, StorageException, ItemNotFoundException, 
            IllegalOperationException
    {
        InputStream[] data = new InputStream[ getRubyGroupRepository().getMemberRepositories().size() ];
        try
        {
            int index = 0;
            for (Repository repository : getRubyGroupRepository().getMemberRepositories())
            {
                RubygemsFacade facade = ((RubyRepository) repository ).getRubygemsFacade();
                StorageFileItem[] deps = facade.prepareDependencies( facade
                        .bundlerDependencies(), gemname );
                try
                {
                    data[index] = deps[0].getInputStream();
                }
                catch (IOException e)
                {
                    throw new LocalStorageException( "errors merging json dependencies for: "
                                                             + gemname,
                                                     e );
                }
                index++;
            }
            return bundlerDependencies.merge( data );
        }
        finally
        {
            for (InputStream is : data)
            {
                IOUtil.close( is );
            }
        }
    }

    protected RubyGroupRepository getRubyGroupRepository()
    {
        return (RubyGroupRepository) repository;
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public StorageFileItem[] prepareDependencies( BundlerDependencies bundlerDependencies, String... gemnames )
            throws AccessDeniedException, IllegalOperationException,
                    ItemNotFoundException, org.sonatype.nexus.proxy.StorageException
    {
        for( Repository repository: getRubyGroupRepository().getMemberRepositories() )
        {
            RubygemsFacade facade = ((RubyRepository) repository).getRubygemsFacade();
            StorageFileItem[] deps = facade.prepareDependencies( facade.bundlerDependencies(),
                                                                 gemnames );
            for( StorageFileItem dep: deps )
            {
                try
                {
                    bundlerDependencies.add( dep.getName(), dep.getInputStream() );
                } 
                catch ( IOException e )
                {
                    throw new LocalStorageException( "errors adding dependencies: " + dep, e );
                }
            }
        }
        return null;
    }
    
}