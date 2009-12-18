package org.sonatype.nexus.ruby.gem;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.yamlbeans.YamlException;
import net.sourceforge.yamlbeans.YamlWriter;

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
    {
        // TODO Auto-generated method stub
        return null;
    }

    public String write( GemSpecification gemspec )
        throws IOException
    {
        return writeGemSpectoYaml( gemspec );
    }

    // ==

    protected String writeGemSpectoYaml( GemSpecification gemspec )
        throws IOException
    {
        // snake has some problems i could not overcome
        // return writeGemSpectoYamlWithSnakeYaml( gemspec );
        // yamlbeans makes better yaml at 1st glance
        return writeGemSpectoYamlWithYamlBeans( gemspec );
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

    private String writeGemSpectoYamlWithYamlBeans( GemSpecification gemspec )
        throws IOException
    {
        Map<String, Class<?>> mapping = new HashMap<String, Class<?>>();
        mapping.put( "ruby/object:Gem::Specification", GemSpecification.class );
        mapping.put( "ruby/object:Gem::Dependency", GemDependency.class );
        mapping.put( "ruby/object:Gem::Requirement", GemRequirement.class );
        mapping.put( "ruby/object:Gem::Version", GemVersion.class );

        StringWriter fw = new StringWriter();

        YamlWriter writer = new YamlWriter( fw );
        writer.getConfig().writeConfig.setWriteDefaultValues( true );
        writer.getConfig().writeConfig.setExplicitFirstDocument( true );
        writer.getConfig().writeConfig.setAlwaysWriteClassname( true );
        for ( Map.Entry<String, Class<?>> entry : mapping.entrySet() )
        {
            writer.getConfig().setClassTag( entry.getKey(), entry.getValue() );
        }

        try
        {
            writer.write( gemspec );
            writer.close();
            return fw.toString();
        }
        catch ( YamlException e )
        {
            IOException e1 = new IOException( e.getMessage() );
            e1.initCause( e );
            throw e1;
        }
    }

}
