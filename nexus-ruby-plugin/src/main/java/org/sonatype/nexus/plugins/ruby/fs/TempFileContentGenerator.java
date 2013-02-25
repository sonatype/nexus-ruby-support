package org.sonatype.nexus.plugins.ruby.fs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.item.ContentGenerator;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.PreparedContentLocator;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.repository.Repository;

@Component( role = ContentGenerator.class, hint = TempFileContentGenerator.ID )
public class TempFileContentGenerator implements ContentGenerator
{

    public static final String BUNLDER_TMP_FILE = "BUNLDER_TMP_FILE";
    public static final String ID = "TempFileContentGenerator";
    
    @Override
    public String getGeneratorId()
    {
        return ID;
    }

    @Override
    public ContentLocator generateContent( Repository repository, String path,
            StorageFileItem item ) throws ItemNotFoundException
    {
        try
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            IOUtil.copy( item.getInputStream(), out );
            out.close();
            // TODO repository.deleteItem( item.getResourceStoreRequest() );
            // mimic repository delete
            File file  = (File) item.getItemContext().get( BUNLDER_TMP_FILE );
            FileUtils.deleteQuietly( file );
            file = new File( file.getAbsolutePath().replace( ".nexus/tmp/", ".nexus/attributes/.nexus/tmp/" ) );
            FileUtils.deleteQuietly( file );
            
            item.setLength( out.size() );
            return new PreparedContentLocator( new ByteArrayInputStream( out.toByteArray() ), "application/x-marshal-ruby" );
        }
        catch ( IOException e )
        {
            throw new ItemNotFoundException( item.getResourceStoreRequest(), e);
        }
    }
}
