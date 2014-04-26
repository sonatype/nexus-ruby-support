package org.sonatype.nexus.ruby;


public class GemFile extends BaseGemFile {
    
    GemFile( Layout layout, String storage, String remote, String filename )
    {
        super( layout, FileType.GEM, storage, remote, filename );
    }
    
    GemFile( Layout layout, String storage, String remote,
             String name, String version, String platform )
    {
        super( layout, FileType.GEM, storage, remote,
               name, version, platform );
    }

    public GemspecFile gemspec(){
        if ( version() != null )
        {
            return layout.gemspecFile( name(), version(), platform() );
        }
        else
        {
            return layout.gemspecFile( filename() );
        }
    }
}