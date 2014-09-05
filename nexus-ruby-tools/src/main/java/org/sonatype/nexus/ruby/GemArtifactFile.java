package org.sonatype.nexus.ruby;

public class GemArtifactFile extends RubygemsFile {
    
    private final String version;
    private final boolean snapshot;
    private GemFile gem;

    GemArtifactFile( RubygemsFileFactory factory, String path,
                     String name, String version, boolean snapshot )
    {
        super( factory, FileType.GEM_ARTIFACT, path, path, name );
        this.version = version;
        this.snapshot = snapshot;
    }

    /**
     * the version of the gem
     * 
     * @return
     */
    public String version()
    {
        return version;
    }
    
    /**
     * whether it is a snapshot or not
     * @return
     */
    public boolean isSnapshot()
    {
        return snapshot;
    }

    /**
     * is lazy state of the associated GemFile. the GemFile needs to
     * have the right platform for which the DependencyData is needed
     * to retrieve this platform. a second call can be done without DependencyData !
     * 
     * @param dependencies can be null
     * @return the associated GemFile - can be null if DependencyData was never passed in
     */
    public GemFile gem( DependencyData dependencies )
    {
        if ( this.gem == null && dependencies != null)
        {
            String platform = dependencies.platform( version() );
            if ( platform != null )
            {
                this.gem = factory.gemFile( name(), version(), platform );
            }
        }
        return this.gem;
    }

    /**
     * the associated DependencyFile object for the gem-artifact
     * @return
     */
    public DependencyFile dependency()
    {
        return factory.dependencyFile( name() );
    }
}