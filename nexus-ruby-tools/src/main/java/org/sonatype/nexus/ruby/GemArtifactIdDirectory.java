package org.sonatype.nexus.ruby;

import java.util.Arrays;

public class GemArtifactIdDirectory extends Directory {
    
    private final boolean prereleased;

    GemArtifactIdDirectory( RubygemsFileFactory factory, String storage, String remote, String name,
                            boolean prereleased )
    {
        super( factory, storage, remote, name );
        items.add( "maven-metadata.xml" );
        items.add( "maven-metadata.xml.sha1" );
        this.prereleased = prereleased;
    }

    public boolean isPrerelease()
    {
        return prereleased;
    }
    
    public void setItems( Directory dir )
    {
        throw new RuntimeException( "not implemented" );
    }
    
    public DependencyFile dependency()
    {
        return this.factory.dependencyFile( name() );
    }
    
    public void setItems( DependencyData data )
    {
        if ( ! prereleased )
        {
            // we list ALL versions when not on prereleased directory
            this.items.addAll( 0, Arrays.asList( data.versions( false ) ) );
        }
        this.items.addAll( 0, Arrays.asList( data.versions( true ) ) );
    }
}