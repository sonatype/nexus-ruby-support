package org.sonatype.nexus.plugins.ruby.fs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.ruby.BundlerDependencies;
import org.sonatype.nexus.ruby.RubygemsGateway;
import org.sonatype.nexus.ruby.SpecsIndexType;

public abstract class AbstractRubygemsFacade implements RubygemsFacade {

    protected final RubygemsGateway gateway;
    protected final RubyRepository repository;
    
    public AbstractRubygemsFacade( RubygemsGateway gateway, RubyRepository repository )
    {
        this.gateway = gateway;
        this.repository = repository;
    }
    
    @Override
    public void addGem( RubyLocalRepositoryStorage storage, StorageFileItem gem ) 
            throws UnsupportedStorageOperationException, LocalStorageException
    {
        throw new UnsupportedStorageOperationException( "can not add gems through this repository: " + repository );
    }
    
    @Override
    public boolean removeGem( RubyLocalRepositoryStorage storage, StorageFileItem gem )
            throws UnsupportedStorageOperationException, LocalStorageException
    {
        throw new UnsupportedStorageOperationException( "can not remove gems through this repository: " + repository );
    }

    @Override
    public void mergeSpecsIndex( RubyLocalRepositoryStorage storage,
            SpecsIndexType type, StorageItem localSpecs, List<StorageItem> specsItems )
            throws UnsupportedStorageOperationException, LocalStorageException, IOException {
        throw new UnsupportedStorageOperationException( "can not merge specs-indeces for this repository: " + repository );
    }

    protected InputStream toGZIPInputStream(StorageFileItem item) throws LocalStorageException {
        try
        {

            return new GZIPInputStream( item.getInputStream() );
        
        }
        catch (IOException e) {
            throw new LocalStorageException( "error getting stream to: " + item, e );
        }
    }

    protected InputStream toInputStream(StorageFileItem item) throws LocalStorageException {
        try
        {

            return item.getInputStream();
        
        }
        catch (IOException e) {
            throw new LocalStorageException( "error getting stream to: " + item, e );
        }
    }

    protected void storeSpecsIndex(RubyRepository repository, RubyLocalRepositoryStorage storage, SpecsIndexType type,
            InputStream newSpecsIndex) throws LocalStorageException,
            UnsupportedStorageOperationException
    {
        if ( newSpecsIndex != null )
        {
            storage.storeSpecsIndex( repository, type, newSpecsIndex );
        }
    }

    @Override
    public RubygemFile deletableFile( String path )
    {
        return RubygemFile.fromFilename( path );
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public InputStream bundlerDependencies( StorageFileItem specs, long modified,
            StorageFileItem prereleasedSpecs, long prereleasedModified,
            File cacheDir, String... gemnames ) 
                    throws ItemNotFoundException, org.sonatype.nexus.proxy.StorageException, IOException {
        BundlerDependencies deps = gateway.newBundlerDependencies( toGZIPInputStream( specs ), modified, 
                toGZIPInputStream( prereleasedSpecs ), prereleasedModified, cacheDir);
        for( String gemname: gemnames )
        {
            
            String[] missing = deps.addDependenciesFor( gemname );
            if ( missing.length > 0 )
            {
                InputStream[] missingSpecs = new InputStream[ missing.length ];
                int index = 0;
                for( String version: missing )
                {
                    try {
                        StorageFileItem item = repository.retrieveGemspec( gemname + "-" + version );
                        missingSpecs[ index ++ ] = item.getInputStream();
                    }
                    // TODO better exception handling
                    catch (Exception e)
                    {
                        throw new RuntimeException( e );
                    }
                }
                deps.updateCache( gemname, missingSpecs );
            }
        }
        return deps.dump();
    }
}