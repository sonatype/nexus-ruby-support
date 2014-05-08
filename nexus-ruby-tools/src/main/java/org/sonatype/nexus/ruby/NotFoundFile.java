package org.sonatype.nexus.ruby;

public class NotFoundFile extends RubygemsFile {
    
    public NotFoundFile( Layout layout, String path )
    {
        super( layout, FileType.NOT_FOUND, null, path, null );
        markAsNotExists();
    }
}