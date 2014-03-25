package org.sonatype.nexus.plugins.ruby.fs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.item.ContentGenerator;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.PreparedContentLocator;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.repository.Repository;

@Singleton
@Named( GunzipContentGenerator.ID )
public class GunzipContentGenerator implements ContentGenerator {

    public static final String ID = "GunzipContentGenerator";
    
    @Override
    public String getGeneratorId() {
        return ID;
    }

    @Override
    public ContentLocator generateContent(Repository repository, String path,
            StorageFileItem item) throws ItemNotFoundException {
       if( true ) throw new RuntimeException( "here" );
       InputStream in = null;
       ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            in = new GZIPInputStream( item.getInputStream() );
            IOUtil.copy( in, out );
            
            return new PreparedContentLocator( new ByteArrayInputStream( out.toByteArray() ), 
                                               "application/x-marshal-ruby",
                                               out.toByteArray().length );
        } catch (IOException e) {
            throw new ItemNotFoundException(item.getResourceStoreRequest(), repository, e);
        }
        finally
        {
            IOUtil.close( in );
            IOUtil.close( out );
        }
    }
}
