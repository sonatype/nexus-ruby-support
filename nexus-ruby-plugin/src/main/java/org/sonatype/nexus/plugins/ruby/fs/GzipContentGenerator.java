package org.sonatype.nexus.plugins.ruby.fs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.item.ContentGenerator;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.PreparedContentLocator;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.repository.Repository;

@Component( role = ContentGenerator.class, hint = GzipContentGenerator.ID )
public class GzipContentGenerator implements ContentGenerator {

    public static final String ID = "GzipContentGenerator";
    
    @Override
    public String getGeneratorId() {
        return ID;
    }

    @Override
    public ContentLocator generateContent(Repository repository, String path,
            StorageFileItem item) throws ItemNotFoundException {
        try {
            ByteArrayOutputStream gzipped = new ByteArrayOutputStream();
            OutputStream out = new GZIPOutputStream( gzipped );
            IOUtil.copy( item.getInputStream(), out );
            out.close();
//            ((DefaultStorageFileItem) item).setModified( item.getModified() );
//            ((DefaultStorageFileItem) item).setCreated( item.getCreated() );
            item.setLength( gzipped.toByteArray().length );

            return new PreparedContentLocator( new ByteArrayInputStream( gzipped.toByteArray() ), "application/x-gzip" );
        } catch (IOException e) {
            throw new ItemNotFoundException(item.getResourceStoreRequest(), e);
        }
    }

}
