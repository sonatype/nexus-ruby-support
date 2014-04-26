package org.sonatype.nexus.ruby;


public class GemspecFile extends BaseGemFile {
    
    GemspecFile( Layout layout, String storage, String remote, String name )
    {
        super( layout, FileType.GEMSPEC, storage, remote, name );
    }

    GemspecFile( Layout layout, String storage, String remote,
                 String name, String version, String platform )
    {
        super( layout, FileType.GEMSPEC, storage, remote,
               name, version, platform );
    }

    public GemFile gem(){
        if ( version() != null )
        {
            return layout.gemFile( name(), version(), platform() );
        }
        else
        {
            return layout.gemFile( filename() );
        }
    }
}