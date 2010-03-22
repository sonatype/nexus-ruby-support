package org.sonatype.nexus.ruby.gem;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.ruby.gem.yaml.MappingConstructor;
import org.sonatype.nexus.ruby.gem.yaml.MappingRepresenter;
import org.yaml.snakeyaml.Dumper;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Loader;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

/**
 * This is here just be able to quickly switch between snakeYaml and YamlBeans, since they are both good, with their own
 * quirks. Not sure yet which to use. YamlBeans makes better looking Yaml.
 * 
 * @author cstamas
 */
@Component( role = GemSpecificationIO.class )
public class DefaultGemSpecificationIO
    implements GemSpecificationIO
{

    public GemSpecification read( String string )
        throws IOException
    {
        return readGemSpecfromYaml( string );
    }

    public String write( GemSpecification gemspec )
        throws IOException
    {
        return writeGemSpectoYaml( gemspec );
    }

    // ==

    protected GemSpecification readGemSpecfromYaml( String gemspecString )
        throws IOException
    {
        // snake has some problems i could not overcome
        // return readGemSpecfromYamlWithSnakeYaml( gemspec );
        // yamlbeans makes better yaml at 1st glance
        return readGemSpecfromYamlWithSnakeYaml( gemspecString );
    }

    protected String writeGemSpectoYaml( GemSpecification gemspec )
        throws IOException
    {
        // snake has some problems i could not overcome
        // return writeGemSpectoYamlWithSnakeYaml( gemspec );
        // yamlbeans makes better yaml at 1st glance
        return writeGemSpectoYamlWithSnakeYaml( gemspec );
    }

    // == SnakeYaml

    protected GemSpecification readGemSpecfromYamlWithSnakeYaml( String gemspecString )
        throws IOException
    {
        Map<String, Class<?>> mapping = new HashMap<String, Class<?>>();
        mapping.put( "!ruby/object:Gem::Specification", GemSpecification.class );
        mapping.put( "!ruby/object:Gem::Dependency", GemDependency.class );
        mapping.put( "!ruby/object:Gem::Requirement", GemRequirement.class );
        mapping.put( "!ruby/object:Gem::Version", GemVersion.class );

        Constructor constructor = new MappingConstructor( mapping );
        Loader loader = new Loader( constructor );

        MappingRepresenter representer = new MappingRepresenter( mapping );
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setExplicitStart( true );
        dumperOptions.setDefaultFlowStyle( DumperOptions.FlowStyle.BLOCK );
        dumperOptions.setDefaultScalarStyle( DumperOptions.ScalarStyle.PLAIN );

        Dumper dumper = new Dumper( representer, dumperOptions );

        Yaml yaml = new Yaml( loader, dumper );

        return (GemSpecification) yaml.load( gemspecString );
    }

    protected String writeGemSpectoYamlWithSnakeYaml( GemSpecification gemspec )
        throws IOException
    {
        Map<String, Class<?>> mapping = new HashMap<String, Class<?>>();
        mapping.put( "!ruby/object:Gem::Specification", GemSpecification.class );
        mapping.put( "!ruby/object:Gem::Dependency", GemDependency.class );
        mapping.put( "!ruby/object:Gem::Requirement", GemRequirement.class );
        mapping.put( "!ruby/object:Gem::Version", GemVersion.class );

        Constructor constructor = new MappingConstructor( mapping );
        Loader loader = new Loader( constructor );

        MappingRepresenter representer = new MappingRepresenter( mapping );
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setExplicitStart( true );
        dumperOptions.setDefaultFlowStyle( DumperOptions.FlowStyle.BLOCK );
        dumperOptions.setDefaultScalarStyle( DumperOptions.ScalarStyle.PLAIN );

        Dumper dumper = new Dumper( representer, dumperOptions );

        Yaml yaml = new Yaml( loader, dumper );

        return yaml.dump( gemspec );
    }

}
