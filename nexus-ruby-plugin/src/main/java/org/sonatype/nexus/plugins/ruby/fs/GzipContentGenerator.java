package org.sonatype.nexus.plugins.ruby.fs;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.ContentGenerator;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.PreparedContentLocator;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.repository.Repository;

public class GzipContentGenerator implements ContentGenerator {

    public static final String ID = "GzipContentGenerator";
    
    @Override
    public String getGeneratorId() {
        return ID;
    }

    @Override
    public ContentLocator generateContent(Repository repository, String path,
            StorageFileItem item) throws IllegalOperationException,
            ItemNotFoundException, StorageException {
        try {
            InputStream is = new GZIPInputStream( item.getInputStream() );
            return new PreparedContentLocator( is, "application/x-gzip" );
        } catch (IOException e) {
            throw new ItemNotFoundException(item.getResourceStoreRequest(), e);
        }
    }

}
