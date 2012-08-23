package org.sonatype.nexus.plugins.ruby.fs;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.DefaultStorageCollectionItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.local.fs.FileContentLocator;

class RubygemsStorageCollectionItem extends DefaultStorageCollectionItem
{

    private File gemsBasedir;
    private Repository repository;

    public RubygemsStorageCollectionItem( Repository repository,
            ResourceStoreRequest request, File gemsBasedir )
    {
        super( repository, request, true, false );
        this.gemsBasedir = gemsBasedir; 
        this.repository = repository;
    }

    public Collection<StorageItem> list()
    {
        Collection<StorageItem> result = new ArrayList<StorageItem>();

        visit( gemsBasedir, result );
        
        correctPaths( result );

        return result;
    }
    
    void visit( File path, Collection<StorageItem> list )
    {
        if( path.exists() && path.isDirectory() )
        {
            for( File f: path.listFiles() ){
                visit( f, list );
            }
        }
        else if( path.getName().endsWith( ".gem" ) )
        {
            String gemPath = "/gems/" + path.getName();
            DefaultStorageFileItem item = new DefaultStorageFileItem( repository, new ResourceStoreRequest( gemPath, true, false ), 
                    true, false, new FileContentLocator( path, "binary/octet-stream" ) );
            item.setLength( path.length() );
            item.setModified( path.lastModified() );
            list.add( item );
        }
    }        
}