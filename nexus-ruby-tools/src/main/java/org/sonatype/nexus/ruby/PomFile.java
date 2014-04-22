package org.sonatype.nexus.ruby;

public class PomFile extends RubygemsFile {
    
    private final String version;
    private final boolean snapshot;

    PomFile( Layout layout, String storage, String remote,
             String name, String version, boolean snapshot )
    {
        super( layout, FileType.POM, storage, remote, name );
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

    public GemspecFile gemspec( Dependencies dependencies )
    {
        String platform = dependencies.platform( version() );
        if ( "ruby".equals( platform ) )
        {
            return layout.gemspecFile( name() + "-" + version() );
        }
        else
        {
            return layout.gemspecFile( name() + "-" + version() + "-" + platform );
        }
    }

    public DependencyFile dependency()
    {
        return layout.dependencyFile( name() );
    }
}