package org.sonatype.nexus.ruby;

public class GemArtifactFile extends RubygemsFile {
    
    private final String version;
    private final boolean snapshot;
    private GemFile gem;

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
        if ( this.gem == null )
        {
            String platform = dependencies.platform( version() );
            if ( platform != null )
            {
                this.gem = layout.gemFile( name(), version(), platform );
            }
        }
        return this.gem;
    }

    public DependencyFile dependency()
    {
        return layout.dependencyFile( name() );
    }
}