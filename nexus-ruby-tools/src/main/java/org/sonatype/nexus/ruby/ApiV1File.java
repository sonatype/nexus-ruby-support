package org.sonatype.nexus.ruby;

public class ApiV1File extends RubygemsFile {
    
    ApiV1File( RubygemsFileFactory factory, String storage, String remote, String name )
    {
        super( factory, FileType.API_V1, storage, remote, name );
        set( null );// no payload
    }

    public RubygemsFile gem( String filename )
    {
        return factory.gemFile( filename );
    }
}