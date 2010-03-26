package org.sonatype.nexus.ruby.gem.yaml;

import org.sonatype.nexus.ruby.gem.GemDependency;
import org.sonatype.nexus.ruby.gem.GemRequirement;
import org.sonatype.nexus.ruby.gem.GemSpecification;
import org.sonatype.nexus.ruby.gem.GemVersion;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;

/**
 * A helper for snakeYaml.
 * 
 * @author cstamas
 */
public class MappingConstructor
    extends Constructor
{
    public MappingConstructor()
    {
        super();

        this.addTypeDescription( new TypeDescription( GemSpecification.class,
                                                      new Tag( "!ruby/object:Gem::Specification" ) ) );
        this.addTypeDescription( new TypeDescription( GemDependency.class, new Tag( "!ruby/object:Gem::Dependency" ) ) );
        this
            .addTypeDescription( new TypeDescription( GemRequirement.class, new Tag( "!ruby/object:Gem::Requirement" ) ) );
        this.addTypeDescription( new TypeDescription( GemVersion.class, new Tag( "!ruby/object:Gem::Version" ) ) );
    }
}
