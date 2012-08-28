package org.sonatype.nexus.plugins.ruby.hosted;

import java.io.InputStream;

import org.sonatype.nexus.plugins.ruby.RubyGateway;
import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.plugins.ruby.fs.AbstractRubygemsFacade;
import org.sonatype.nexus.plugins.ruby.fs.GzipContentGenerator;
import org.sonatype.nexus.plugins.ruby.fs.RubyLocalRepositoryStorage;
import org.sonatype.nexus.plugins.ruby.fs.SpecsIndexType;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;

public class HostedRubygemsFacade extends AbstractRubygemsFacade
{

    public HostedRubygemsFacade( RubyGateway gateway, RubyRepository repository )
    {
        super( gateway, repository );
    }

    @Override
    public void addGem( RubyLocalRepositoryStorage storage, StorageFileItem gem ) 
            throws UnsupportedStorageOperationException, LocalStorageException
    {
        try
        {
            Object spec = gateway.spec( toInputStream( gem ) );
            for ( SpecsIndexType type : SpecsIndexType.values() )
            {
                StorageFileItem specsIndex = retrieveSpecsIndex( repository, storage, type );
                InputStream newSpecsIndex = gateway.addSpec( spec, toInputStream( specsIndex ), type );
                if ( newSpecsIndex != null )
                {
                    storage.storeSpecsIndex(repository, type, newSpecsIndex);
                }
            }
        }
        catch (ItemNotFoundException e)
        {
            throw new LocalStorageException( "error updating rubygems index", e );
        }
    }
    
    @Override
    public void removeGem( RubyLocalRepositoryStorage storage, StorageFileItem gem )
            throws UnsupportedStorageOperationException, LocalStorageException
    {
        try
        {
            Object spec = gateway.spec( toInputStream( gem ) );
            for ( SpecsIndexType type : SpecsIndexType.values() )
            {
                // assume specs-index exists since gem-file does
                StorageFileItem specsIndex = storage.retrieveSpecsIndex( repository, type, false );
                InputStream newSpecsIndex = gateway.deleteSpec( spec, toInputStream( specsIndex ) );
                
                if ( newSpecsIndex != null )
                {
                    storage.storeSpecsIndex( repository, type, newSpecsIndex );
                }
            }
        }
        catch (ItemNotFoundException e)
        {
            throw new LocalStorageException( "error updating rubygems index", e );
        }
    }

    private void createEmptySpecs( RubyLocalRepositoryStorage storage, SpecsIndexType type ) 
            throws LocalStorageException
    {
        try
        {
        
            // create an empty index
            storage.storeSpecsIndex(repository, type, gateway.emptyIndex() );
    
        }
        catch (UnsupportedStorageOperationException ee )
        {
            throw new LocalStorageException( "could not store empty specs-index", ee );
        }
    }
    
    @Override
    public StorageFileItem retrieveSpecsIndex( RubyLocalRepositoryStorage storage, 
            SpecsIndexType type, boolean gzipped ) 
            throws ItemNotFoundException, LocalStorageException
    {
        StorageFileItem result = retrieveSpecsIndex(repository, storage, type);

        if ( gzipped )
        {
            result.setContentGeneratorId( GzipContentGenerator.ID );
        }
        return result;
    }
    
    private StorageFileItem retrieveSpecsIndex( RubyRepository repository, RubyLocalRepositoryStorage storage, 
            SpecsIndexType type ) 
            throws ItemNotFoundException, LocalStorageException
    {
        try
        {
        
            return storage.retrieveSpecsIndex( repository, type, false );

        }
        catch ( ItemNotFoundException e )
        {
            // create an empty index
            createEmptySpecs( storage, type );
                
            // now return the new empty index
            return storage.retrieveSpecsIndex( repository, type, false );
        }
    }
}