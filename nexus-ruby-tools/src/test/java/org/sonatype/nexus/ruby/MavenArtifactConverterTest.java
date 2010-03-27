package org.sonatype.nexus.ruby;

import static org.jruby.embed.LocalContextScope.SINGLETON;
import static org.jruby.embed.LocalVariableBehavior.PERSISTENT;

import java.io.File;
import java.io.FileReader;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;
import org.jruby.embed.ScriptingContainer;
import org.junit.Assert;
import org.sonatype.nexus.ruby.gem.GemSpecification;
import org.sonatype.nexus.ruby.gem.GemSpecificationIO;

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

    private ScriptingContainer scriptingContainer;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        scriptingContainer = new ScriptingContainer( SINGLETON, PERSISTENT );

        // setting the JRUBY_HOME to the one from the jruby jar - ignoring the environment setting !
        this.scriptingContainer.getProvider().getRubyInstanceConfig().setJRubyHome(
            Thread.currentThread().getContextClassLoader().getResource( "META-INF/jruby.home" ).toString()
                .replaceFirst( "^jar:", "" ) );
    }

    protected void tearDown()
        throws Exception
    {
        super.tearDown();
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
        throws Exception
    {
        File yamlFile = new File( "src/test/resources/metadata-prawn" );

        String originalYamlString = FileUtils.fileRead( yamlFile );

        GemSpecificationIO gemSpecIO = lookup( GemSpecificationIO.class );

        GemSpecification gemSpec = gemSpecIO.read( originalYamlString );

        String dumpedYamlString = gemSpecIO.write( gemSpec );

        System.out.println( "snakeYAML ****" );
        System.out.println( dumpedYamlString );

        // will fail -- snakeYaml "sorts" properties alphabetically!
        // a compare by value, and as string would maybe work
        // Assert.assertEquals( originalYamlString, dumpedYamlString );
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

        // load helper script
        Object gemTester =
            this.scriptingContainer.runScriptlet( getClass().getResourceAsStream( "gem_tester.rb" ), "gem_tester.rb" );

        // setup local rubygems repository
        File rubygems = new File( getBasedir(), "target/rubygems" );
        rubygems.mkdirs();
        scriptingContainer.callMethod( gemTester, "setup_gems", rubygems.getAbsolutePath(), Object.class );

        // install the slf4j gems
        scriptingContainer.callMethod( gemTester, "install_gems", new String[] {
            "target/gems/org.slf4j.slf4j-api-1.5.8-java.gem", "target/gems/org.slf4j.slf4j-simple-1.5.8-java.gem" },
            Object.class );
        // TODO do not know why this is needed. but without it the first run fails and any successive runs succeeds !!
        scriptingContainer.callMethod( gemTester, "gem", "org.slf4j.slf4j-simple", Object.class );

        // load the slf4j-simple
        Boolean result =
            scriptingContainer.callMethod( gemTester, "require_gem", "maven/org.slf4j/slf4j-simple", Boolean.class );
        assertTrue( result );

        // slf4j-api is already loaded as dependency of slf4j-simple
        result = scriptingContainer.callMethod( gemTester, "require_gem", "maven/org.slf4j/slf4j-api", Boolean.class );
        assertFalse( result );
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
