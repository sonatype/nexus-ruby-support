package org.sonatype.nexus.ruby.gem.yaml;

import org.sonatype.nexus.ruby.gem.GemDependency;
import org.sonatype.nexus.ruby.gem.GemRequirement;
import org.sonatype.nexus.ruby.gem.GemSpecification;
import org.sonatype.nexus.ruby.gem.GemVersion;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

/**
 * A helper for snakeYaml.
 * 
 * @author cstamas
 */
public class MappingRepresenter
    extends Representer
{
    public MappingRepresenter()
    {
        super();

        this.nullRepresenter = new RepresentNull();

        this.addClassTag( GemSpecification.class, new Tag( "!ruby/object:Gem::Specification" ) );
        this.addClassTag( GemDependency.class, new Tag( "!ruby/object:Gem::Dependency" ) );
        this.addClassTag( GemRequirement.class, new Tag( "!ruby/object:Gem::Requirement" ) );
        this.addClassTag( GemVersion.class, new Tag( "!ruby/object:Gem::Version" ) );
    }

    private class RepresentNull
        implements Represent
    {
        public Node representData( Object data )
        {
            return representScalar( Tag.NULL, "null" );
        }
    }
}
