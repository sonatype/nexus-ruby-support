package org.sonatype.nexus.ruby;


public class GemspecFile extends BaseGemFile {
    
    GemspecFile( Layout layout, String storage, String remote, String name )
    {
        super( layout, FileType.GEMSPEC, storage, remote, name );
    }

    public GemFile gem(){
        return layout.gemFile( nameWithVersion() );
    }        
}