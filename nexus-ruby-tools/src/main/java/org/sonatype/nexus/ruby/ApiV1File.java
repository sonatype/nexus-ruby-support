package org.sonatype.nexus.ruby;

/**
 * there are currently only two supported files inside the /api/v1 directory: gems and api_key.
 * the constructor allows all file-names
 * 
 * @author christian
 *
 */
public class ApiV1File extends RubygemsFile {
    
    ApiV1File( RubygemsFileFactory factory, String storage, String remote, String name )
    {
        super( factory, FileType.API_V1, storage, remote, name );
        set( null );// no payload
    }

    /**
     * convenient method to convert a gem-filename into <code>GemFile</code>
     * @param filename of the gem
     * @return GemFile
     */
    public GemFile gem( String gemFilename )
    {
        return factory.gemFile( gemFilename.replaceFirst( ".gem$", "" ) );
    }
}