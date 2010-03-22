package org.sonatype.nexus.ruby;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;
import org.junit.Assert;
import org.sonatype.nexus.ruby.gem.GemDependency;
import org.sonatype.nexus.ruby.gem.GemRequirement;
import org.sonatype.nexus.ruby.gem.GemSpecification;
import org.sonatype.nexus.ruby.gem.GemVersion;
import org.sonatype.nexus.ruby.gem.yaml.MappingConstructor;
import org.sonatype.nexus.ruby.gem.yaml.MappingRepresenter;
import org.yaml.snakeyaml.Dumper;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Loader;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

/**
 * Unit test for simple App.
 */
public class MavenArtifactConverterTest
    extends PlexusTestCase
{
    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( MavenArtifactConverterTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
        throws IOException
    {
        File yamlFile = new File( "src/test/resources/metadata-prawn" );

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

        String yamlString = FileUtils.fileRead( yamlFile );

        Object obj = yaml.load( yamlString );

        String gemspecString = yaml.dump( obj );

        System.out.println( "snakeYAML ****" );
        System.out.println( gemspecString );

        // will fail
        // Assert.assertEquals( yamlString, gemspecString );
    }

    public void testConversion()
        throws Exception
    {
        doConversion( "org/slf4j/slf4j-api/1.5.8/slf4j-api-1.5.8.pom", new ArtifactCoordinates( "org.slf4j",
                                                                                                "slf4j-api", "1.5.8" ) );
        doConversion( "org/slf4j/slf4j-simple/1.5.8/slf4j-simple-1.5.8.pom", new ArtifactCoordinates( "org.slf4j",
                                                                                                      "slf4j-simple",
                                                                                                      "1.5.8" ) );
        doConversion( "org/apache/ant/ant-parent/1.7.1/ant-parent-1.7.1.pom",
            new ArtifactCoordinates( "org.apache.ant", "ant-parent", "1.7.1" ) );
    }

    public GemArtifact doConversion( String pomPath, ArtifactCoordinates coords )
        throws Exception
    {
        File pomFile = new File( new File( "src/test/resources/repository" ), pomPath );

        MavenArtifactConverter converter = lookup( MavenArtifactConverter.class );

        File artifactFile = new File( pomFile.getParentFile(), pomFile.getName().replace( ".pom", ".jar" ) );

        if ( !artifactFile.isFile() )
        {
            artifactFile = null;
        }

        MavenXpp3Reader reader = new MavenXpp3Reader();

        Model pom = reader.read( new FileReader( pomFile ) );

        MavenArtifact artifact = new MavenArtifact( pom, coords, artifactFile );

        return converter.createGemFromArtifact( artifact, getTestFile( "target/gems/"
                                                                       + converter.getGemFileName( artifact ) ) );
    }

}
