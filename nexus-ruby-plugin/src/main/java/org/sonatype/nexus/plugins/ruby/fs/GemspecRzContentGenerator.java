package org.sonatype.nexus.plugins.ruby.fs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.plugins.ruby.JRubyRubyGateway;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.ContentGenerator;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.PreparedContentLocator;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.local.fs.DefaultFSLocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.local.fs.FileContentLocator;

@Component( role = ContentGenerator.class, hint = GemspecRzContentGenerator.ID )
public class GemspecRzContentGenerator implements ContentGenerator {
    
    public static final String ID = "GemspecRzContentGenerator";

    private final JRubyRubyGateway gateway = new JRubyRubyGateway();

    @Override
    public String getGeneratorId()
    {
        return ID;
    }

    @Override
    public ContentLocator generateContent( Repository repository, String path, StorageFileItem item )
        throws IllegalOperationException, ItemNotFoundException, StorageException
    {
        try
        {
            String name = FileUtils.filename( item.getPath() ).replace( "spec.rz", "" );
            StorageFileItem gemItem = (StorageFileItem) repository.retrieveItem( new ResourceStoreRequest( "/gems/" + name ) );
            File gemPath = ((FileContentLocator) gemItem.getContentLocator()).getFile();

            // adjust metadata to follow the generated content
            ((DefaultStorageFileItem) item).setModified( gemItem.getModified() );
            ((DefaultStorageFileItem) item).setCreated( gemItem.getCreated() );
            item.setLength( gateway.createGemspecRz( gemPath.getAbsolutePath() ).available() );
            
            return new PreparedContentLocator( gateway.createGemspecRz( gemPath.getAbsolutePath() ), 
                    "application/x-ruby-marshal" );
        } 
        catch ( IOException e )
        {
            throw new ItemNotFoundException( item.getResourceStoreRequest(), e );
        }
        catch ( AccessDeniedException e ) 
        {
            throw new ItemNotFoundException( item.getResourceStoreRequest(), e );            
        }
    }
}