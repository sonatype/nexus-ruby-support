package org.sonatype.nexus.ruby;

public class GemArtifactFile extends RubygemsFile {
    
    private final String version;
    private final boolean snapshot;

    GemArtifactFile( Layout layout, String storage, String remote,
                     String name, String version, boolean snapshot )
    {
        super( layout, FileType.GEM_ARTIFACT, storage, remote, name );
        this.version = version;
        this.snapshot = snapshot;
    }

    public String version()
    {
        return version;
    }
    
    public boolean isSnapshot()
    {
        return snapshot;
    }

    public GemFile gem( DependencyData dependencies )
    {
        String platform = dependencies.platform( version() );
        return layout.gemFile( name(), version(), platform );
//        if ( "ruby".equals( platform ) )
//        {
//            return layout.gemFile( name() + "-" + version() );
//        }
//        else
//        {
//            return layout.gemFile( name() + "-" + version() + "-" + platform );
//        }
    }

    public DependencyFile dependency()
    {
        return layout.dependencyFile( name() );
    }
}