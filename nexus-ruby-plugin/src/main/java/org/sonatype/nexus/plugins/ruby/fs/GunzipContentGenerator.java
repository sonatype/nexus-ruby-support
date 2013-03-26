package org.sonatype.nexus.plugins.ruby.fs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.item.ContentGenerator;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.PreparedContentLocator;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.repository.Repository;

@Component( role = ContentGenerator.class, hint = GunzipContentGenerator.ID )
public class GunzipContentGenerator implements ContentGenerator {

    public static final String ID = "GunzipContentGenerator";
    
    @Override
    public String getGeneratorId() {
        return ID;
    }

    @Override
    public ContentLocator generateContent(Repository repository, String path,
            StorageFileItem item) throws ItemNotFoundException {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = new GZIPInputStream( item.getInputStream() );
            IOUtil.copy( in, out );
            out.close();
            in.close();
            item.setLength( out.toByteArray().length );

            return new PreparedContentLocator( new ByteArrayInputStream( out.toByteArray() ), "application/x-marshal-ruby" );
        } catch (IOException e) {
            throw new ItemNotFoundException(item.getResourceStoreRequest(), e);
        }
    }
}
