package org.sonatype.nexus.ruby;

public class NotFoundFile extends RubygemsFile {
    
    public NotFoundFile( Layout layout )
    {
        super( layout, FileType.NOT_FOUND, null, null, null );
    }
}