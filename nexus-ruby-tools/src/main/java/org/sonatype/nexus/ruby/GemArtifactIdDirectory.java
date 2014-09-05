package org.sonatype.nexus.ruby;

import java.util.Arrays;

/**
 * represent /maven/releases/rubygems/{artifactId} or /maven/prereleases/rubygems/{artifactId}
 * 
 * @author christian
 *
 */
public class GemArtifactIdDirectory extends Directory {
    
    private final boolean prereleased;

    GemArtifactIdDirectory( RubygemsFileFactory factory, String path, String name,
                            boolean prereleased )
    {
        super( factory, path, name );
        items.add( "maven-metadata.xml" );
        items.add( "maven-metadata.xml.sha1" );
        this.prereleased = prereleased;
    }

    /**
     * whether to show prereleased or released gems inside the directory 
     * @return
     */
    public boolean isPrerelease()
    {
        return prereleased;
    }

    /**
     * the <code>DependencyFile</code> of the given gem
     * @return
     */
    public DependencyFile dependency()
    {
        return this.factory.dependencyFile( name() );
    }
    
    /**
     * setup the directory items. for each version one item, either
     * released or prereleased version.
     * 
     * @param data
     */
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