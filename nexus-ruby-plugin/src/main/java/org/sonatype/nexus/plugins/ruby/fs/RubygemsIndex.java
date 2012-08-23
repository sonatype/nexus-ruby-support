package org.sonatype.nexus.plugins.ruby.fs;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import org.sonatype.nexus.plugins.ruby.JRubyRubyGateway;
import org.sonatype.nexus.plugins.ruby.RubyGroupRepository;
import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.storage.local.fs.FileContentLocator;

public class RubygemsIndex {
    
    private final static Map<String, String> emptyUserAttributes = Collections.emptyMap();
    
    private RubyRepository repository;

    private final JRubyRubyGateway gateway = new JRubyRubyGateway();
    
    RubygemsIndex(RubyRepository repository)
    {
        this.repository = repository;
    }
    
    private File getFile( StorageFileItem file )
    {
       return ((FileContentLocator)file.getContentLocator()).getFile();
    }

    void merge( RubyGroupRepository repository, SpecsIndexType type )
    {
        
    }
    
    void add( StorageFileItem gem ) 
            throws StorageException, AccessDeniedException, ItemNotFoundException, 
                IllegalOperationException, UnsupportedStorageOperationException
    {
        Object spec = gateway.spec( getFile( gem ) );
        for ( SpecsIndexType type : SpecsIndexType.values() )
        {
            StorageFileItem specs = retrieveSpecs( repository, type );
            repository.storeItem( 
                    specs.getResourceStoreRequest(), 
                    gateway.addSpec( spec, getFile( specs ), type ), 
                    specs.getRepositoryItemAttributes().asMap()
                    );
        }
    }
    
    void remove( StorageFileItem gem )
            throws StorageException, AccessDeniedException, UnsupportedStorageOperationException,
                ItemNotFoundException, IllegalOperationException
    {
        Object spec = gateway.spec( getFile( gem ) );
        for ( SpecsIndexType type : SpecsIndexType.values() )
        {
            StorageFileItem specs = retrieveSpecs( repository, type );
            repository.storeItem( 
                    specs.getResourceStoreRequest(), 
                    gateway.deleteSpec( spec, getFile( specs ) ), 
                    specs.getRepositoryItemAttributes().asMap()
                    );
        }
    }
    
    public StorageFileItem retrieveSpecs( RubyRepository repository, SpecsIndexType type ) 
            throws StorageException, AccessDeniedException, ItemNotFoundException, 
                IllegalOperationException, UnsupportedStorageOperationException
    {
        ResourceStoreRequest request = new ResourceStoreRequest( type.filename() );
        try
        {
            
            return (StorageFileItem) repository.retrieveItem( request );
            
        }
        catch ( ItemNotFoundException e )
        {
            // create an empty index
            repository.storeItem( request, gateway.emptyIndex(), emptyUserAttributes );
            
            // now return the new empty index
            return (StorageFileItem) repository.retrieveItem( new ResourceStoreRequest( type.filename() ) );
        }
    }

    public StorageFileItem retrieveSpecsGz( RubyRepository repository, SpecsIndexType type ) 
            throws StorageException, AccessDeniedException, ItemNotFoundException, 
                IllegalOperationException, UnsupportedStorageOperationException
    {
        StorageFileItem item = retrieveSpecs( repository, type );
        item.setContentGeneratorId( GzipContentGenerator.ID );
        return item;
    }
    
}