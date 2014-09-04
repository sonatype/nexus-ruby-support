package org.sonatype.nexus.ruby;


public class GemFile extends BaseGemFile {
    
    GemFile( RubygemsFileFactory factory, String storage, String remote, String filename )
    {
        super( factory, FileType.GEM, storage, remote, filename );
    }
    
    GemFile( RubygemsFileFactory factory, String storage, String remote,
             String name, String version, String platform )
    {
        super( factory, FileType.GEM, storage, remote,
               name, version, platform );
    }

    public GemspecFile gemspec(){
        if ( version() != null )
        {
            return factory.gemspecFile( name(), version(), platform() );
        }
        else
        {
            return factory.gemspecFile( filename() );
        }
    }
}