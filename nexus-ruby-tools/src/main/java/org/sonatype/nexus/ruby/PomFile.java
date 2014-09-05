package org.sonatype.nexus.ruby;

public class PomFile extends RubygemsFile {
    
    private final String version;
    private final boolean snapshot;

    PomFile( RubygemsFileFactory factory, String path,
              String name, String version, boolean snapshot )
    {
        super( factory, FileType.POM, path, path, name );
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

    public GemspecFile gemspec( DependencyData dependencies )
    {
        String platform = dependencies.platform( version() );
        return factory.gemspecFile( name(), version(), platform );
    }

    public DependencyFile dependency()
    {
        return factory.dependencyFile( name() );
    }
}