package org.sonatype.nexus.ruby.gem;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.gzip.GZipArchiver;
import org.codehaus.plexus.archiver.tar.TarArchiver;
import org.codehaus.plexus.archiver.tar.TarArchiver.TarCompressionMethod;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.FileUtils;

@Component( role = GemPackager.class )
public class DefaultGemPackager
    implements GemPackager
{
    @Requirement
    private GemSpecificationIO gemSpecificationIO;

    public void createGem( GemSpecification gemspec, Collection<GemFileEntry> filesToAdd, File gemFile )
        throws IOException
    {
        File gemWorkdir =
            new File( File.createTempFile( "nexus-gem-work", ".tmp" ).getParentFile(), "wd-"
                + System.currentTimeMillis() );

        gemWorkdir.mkdirs();

        for ( GemFileEntry entry : filesToAdd )
        {
            if ( !entry.getSource().isFile() )
            {
                throw new IOException( "The GEM entry must be a file!" );
            }

            gemspec.getFiles().add( entry.getPathInGem() );

            if ( entry.isOnLoadPath() )
            {
                // we should ensure it's folder (usually "lib/" is here)
            }
        }

        // get YAML
        String gemspecString = gemSpecificationIO.write( gemspec );

        // DEBUG
        // FileUtils.fileWrite( gemFile.getAbsolutePath(), gemspecString );
        // DEBUG

        try
        {
            // write file "metadata" (YAML of gemspec)
            File metadata = new File( gemWorkdir, "metadata" );
            File metadataGz = new File( gemWorkdir, "metadata.gz" );
            FileUtils.fileWrite( metadata.getAbsolutePath(), "UTF-8", gemspecString );
            // gzip it into metadata.gz
            GZipArchiver gzip = new GZipArchiver();
            gzip.setDestFile( metadataGz );
            gzip.addFile( metadata, "metadata.gz" );
            gzip.createArchive();

            // tar.gz the content into data.tar.gz
            File dataTarGz = new File( gemWorkdir, "data.tar.gz" );
            TarArchiver tar = new TarArchiver();
            TarCompressionMethod compression = new TarCompressionMethod();
            compression.setValue( "gzip" );
            tar.setCompression( compression );
            tar.setDestFile( dataTarGz );
            for ( GemFileEntry entry : filesToAdd )
            {
                tar.addFile( entry.getSource(), entry.getPathInGem() );
            }
            tar.createArchive();

            // and finally create gem by tar.gz-ing data.tar.gz and metadata.gz
            tar.setDestFile( gemFile );
            compression.setValue( "none" );
            tar.setCompression( compression );
            tar.addFile( dataTarGz, dataTarGz.getName() );
            tar.addFile( metadataGz, metadataGz.getName() );
            tar.createArchive();
        }
        catch ( ArchiverException e )
        {
            IOException ioe = new IOException( e.getMessage() );
            ioe.initCause( e );
            throw ioe;
        }
        finally
        {
            FileUtils.forceDelete( gemWorkdir );
        }
    }
}
