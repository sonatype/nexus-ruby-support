package org.sonatype.nexus.ruby.gem;

import java.io.IOException;

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
 * quirks. SnakeYaml won ;) So we can clear up this later.
 * 
 * @author cstamas
 */
@Component( role = GemSpecificationIO.class )
public class DefaultGemSpecificationIO
    implements GemSpecificationIO
{
    protected Yaml _yaml;

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

    protected Yaml getYaml()
    {
        if ( _yaml == null )
        {
            Constructor constructor = new MappingConstructor();
            Loader loader = new Loader( constructor );
            MappingRepresenter representer = new MappingRepresenter();

            DumperOptions dumperOptions = new DumperOptions();
            dumperOptions.setExplicitStart( true );
            dumperOptions.setDefaultFlowStyle( DumperOptions.FlowStyle.BLOCK );
            dumperOptions.setDefaultScalarStyle( DumperOptions.ScalarStyle.SINGLE_QUOTED );

            Dumper dumper = new Dumper( representer, dumperOptions );

            _yaml = new Yaml( loader, dumper );
        }

        return _yaml;
    }

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
        return (GemSpecification) getYaml().load( gemspecString );
    }

    protected String writeGemSpectoYamlWithSnakeYaml( GemSpecification gemspec )
        throws IOException
    {
        return getYaml().dump( gemspec );
    }

}
