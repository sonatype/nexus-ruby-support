package org.sonatype.nexus.plugins.ruby.fs;

import java.io.IOException;
import java.io.InputStream;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.ContentGenerator;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.PreparedContentLocator;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.ruby.DefaultRubygemsGateway;
import org.sonatype.nexus.ruby.RubygemsGateway;

@Component( role = ContentGenerator.class, hint = GemspecRzContentGenerator.ID )
public class GemspecRzContentGenerator implements ContentGenerator {
    
    public static final String ID = "GemspecRzContentGenerator";

    private final RubygemsGateway gateway = new DefaultRubygemsGateway();

    @Override
    public String getGeneratorId()
    {
        return ID;
    }

    @Override
    public ContentLocator generateContent( Repository repository, String path, StorageFileItem item )
        throws IllegalOperationException, ItemNotFoundException
    {
        try
        {
            String name = FileUtils.filename( item.getPath() ).replace( "spec.rz", "" );
            StorageFileItem gemItem = (StorageFileItem) repository.retrieveItem( new ResourceStoreRequest( "/gems/" + name ) );

            // adjust metadata to follow the generated content
            ((DefaultStorageFileItem) item).setModified( gemItem.getModified() );
            ((DefaultStorageFileItem) item).setCreated( gemItem.getCreated() );
            InputStream is = gateway.createGemspecRz( gemItem.getInputStream() );
            item.setLength( is.available() );
            
            return new PreparedContentLocator( is, "application/x-ruby-marshal" );
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