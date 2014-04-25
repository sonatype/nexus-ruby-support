package org.sonatype.nexus.ruby;


public class Sha1File extends RubygemsFile {
    
    private final RubygemsFile source;
    
    Sha1File( Layout layout, String storage, String remote, RubygemsFile source )
    {
        super( layout, FileType.SHA1, storage, remote, source.name() );
        this.source = source;
    }

    public RubygemsFile getSource()
    {
        return source;
    }
}