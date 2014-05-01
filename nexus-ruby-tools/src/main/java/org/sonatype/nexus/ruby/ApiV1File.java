package org.sonatype.nexus.ruby;

public class ApiV1File extends RubygemsFile {
    
    ApiV1File( Layout layout, String storage, String remote, String name )
    {
        super( layout, FileType.API_V1, storage, remote, name );
    }

    public RubygemsFile gem( String filename )
    {
        return layout.gemFile( filename );
    }
}