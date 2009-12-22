package org.sonatype.nexus.plugins.ruby;

import java.io.File;
import java.io.IOException;

import org.apache.maven.model.Model;
import org.sonatype.nexus.ruby.MavenArtifact;

/**
 * This component is the central "ruby interaction" point, and is meant to focus all "ruby related" calls to make it
 * easy to swap out and use proper stuff instead. What we have now is POC nexus-ruby-tools, and gemGenerateIndexes is
 * not implemented. The "proper" stuff should use JRuby and invoke the proper Gem:: classes doing the actual work.
 * 
 * @author cstamas
 */
public interface RubyGateway
{
    String getGemFileName( Model pom );

    void createGemFromArtifact( MavenArtifact mart, File target )
        throws IOException;

    void createAndWriteGemspec( Model pom, File target )
        throws IOException;

    void gemGenerateIndexes( File basedir );
}
