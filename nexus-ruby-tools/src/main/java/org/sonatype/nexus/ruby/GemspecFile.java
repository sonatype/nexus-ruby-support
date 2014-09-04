package org.sonatype.nexus.ruby;


public class GemspecFile extends BaseGemFile {
    
    GemspecFile( RubygemsFileFactory factory, String storage, String remote, String name )
    {
        super( factory, FileType.GEMSPEC, storage, remote, name );
    }

    GemspecFile( RubygemsFileFactory factory, String storage, String remote,
                 String name, String version, String platform )
    {
        super( factory, FileType.GEMSPEC, storage, remote,
               name, version, platform );
    }

    public GemFile gem(){
        if ( version() != null )
        {
            return factory.gemFile( name(), version(), platform() );
        }
        else
        {
            return factory.gemFile( filename() );
        }
    }
}