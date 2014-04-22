package org.sonatype.nexus.ruby;


public class GemFile extends BaseGemFile {
    
    GemFile( Layout layout, String storage, String remote, String name )
    {
        super( layout, FileType.GEM, storage, remote, name );
    }

    public GemspecFile gemspec(){
        return layout.gemspecFile( nameWithVersion() );
    }

    public DependencyFile dependency(){
        return layout.dependencyFile( name() );
    }
}