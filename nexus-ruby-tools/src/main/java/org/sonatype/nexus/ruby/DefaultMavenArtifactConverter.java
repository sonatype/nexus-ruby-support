package org.sonatype.nexus.ruby;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Developer;
import org.apache.maven.model.License;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.ruby.gem.GemDependency;
import org.sonatype.nexus.ruby.gem.GemFileEntry;
import org.sonatype.nexus.ruby.gem.GemPackager;
import org.sonatype.nexus.ruby.gem.GemRequirement;
import org.sonatype.nexus.ruby.gem.GemSpecification;
import org.sonatype.nexus.ruby.gem.GemVersion;

/**
 * This is full of "workarounds" here, since for true artifact2gem conversion I would need interpolated POM!
 * 
 * @author cstamas
 */
@Component( role = MavenArtifactConverter.class )
public class DefaultMavenArtifactConverter
    implements MavenArtifactConverter
{
    /**
     * The Java platform key.
     */
    String PLATFORM_JAVA = "java";

    @Requirement
    private GemPackager gemPackager;

    private Maven2GemVersionConverter maven2GemVersionConverter = new Maven2GemVersionConverter();

    public boolean canConvert( MavenArtifact artifact )
    {
        // TODO: this is where we filter currently what to convert.
        // for now, we convert only POMs with packaging "pom", or "jar" (but we ensure there is the primary artifact JAR
        // also
        // RELAXING: doing "pom" packagings but also anything that has primary artifact ending with ".jar".
        if ( canConvert( artifact, "pom", null ) )
        {
            return true;
        }

        if ( canConvert( artifact, artifact.getPom().getPackaging(), "jar" ) )
        {
            return true;
        }

        return false;
    }

    private boolean canConvert( MavenArtifact artifact, String packaging, String extension )
    {
        String fixedExtension = null;

        if ( extension != null )
        {
            fixedExtension = extension.startsWith( "." ) ? extension : "." + extension;
        }

        return StringUtils.equals( packaging, artifact.getPom().getPackaging() )
            && ( ( extension == null && artifact.getArtifactFile() == null ) || ( extension != null
                && artifact.getArtifactFile() != null && artifact.getArtifactFile().getName().endsWith( fixedExtension ) ) );
    }

    public String getGemFileName( MavenArtifact artifact )
    {
        return getGemFileName( artifact.getCoordinates().getGroupId(), artifact.getCoordinates().getArtifactId(),
            artifact.getCoordinates().getVersion(), PLATFORM_JAVA );
    }

    public GemSpecification createSpecification( MavenArtifact artifact )
    {
        GemSpecification result = new GemSpecification();

        // this is fix
        result.setPlatform( PLATFORM_JAVA );

        // the must ones
        result.setName( createGemName( artifact.getCoordinates().getGroupId(), artifact.getCoordinates()
            .getArtifactId(), artifact.getCoordinates().getVersion() ) );
        result.setVersion( new GemVersion( createGemVersion( artifact.getCoordinates().getVersion() ) ) );

        // dependencies
        if ( artifact.getPom().getDependencies().size() > 0 )
        {
            for ( Dependency dependency : artifact.getPom().getDependencies() )
            {
                result.getDependencies().add( convertDependency( artifact, dependency ) );
            }
        }

        // and other stuff "nice to have"
        result.setDate( new Date() ); // now
        result.setDescription( sanitizeStringValue( artifact.getPom().getDescription() != null ? artifact.getPom()
            .getDescription() : artifact.getPom().getName() ) );
        result.setSummary( sanitizeStringValue( artifact.getPom().getName() ) );
        result.setHomepage( sanitizeStringValue( artifact.getPom().getUrl() ) );

        if ( artifact.getPom().getLicenses().size() > 0 )
        {
            for ( License license : artifact.getPom().getLicenses() )
            {
                result.getLicenses().add( sanitizeStringValue( license.getName() + " (" + license.getUrl() + ")" ) );
            }
        }
        if ( artifact.getPom().getDevelopers().size() > 0 )
        {
            for ( Developer developer : artifact.getPom().getDevelopers() )
            {
                result.getAuthors()
                    .add( sanitizeStringValue( developer.getName() + " (" + developer.getEmail() + ")" ) );
            }
        }

        // by default, we pack into lib/ inside gem (where is the jar and the stub ruby)
        result.getRequire_paths().add( "lib" );
        return result;
    }

    public GemArtifact createGemFromArtifact( MavenArtifact artifact, File target )
        throws IOException
    {
        GemSpecification gemspec = createSpecification( artifact );

        if ( target == null )
        {
            throw new IOException( "Must specify target file, where to generate Gem!" );
        }

        // create "meta" ruby file
        File rubyStubFile = generateRubyStub( gemspec, artifact );
        String rubyStubPath = "lib/" + gemspec.getName() + ".rb";

        ArrayList<GemFileEntry> entries = new ArrayList<GemFileEntry>();
        if ( artifact.getArtifactFile() != null )
        {
            entries.add( new GemFileEntry( artifact.getArtifactFile(), true ) );
        }
        entries.add( new GemFileEntry( rubyStubFile, rubyStubPath, true ) );

        // write file
        gemPackager.createGem( gemspec, entries, target );

        return new GemArtifact( gemspec, target );
    }

    // ==

    protected String sanitizeStringValue( String val )
    {
        if ( val == null )
        {
            return null;
        }

        // for now, just to overcome the JRuby 1.4 Yaml parse but revealed by
        // this POM: http://repo1.maven.org/maven2/org/easytesting/fest-assert/1.0/fest-assert-1.0.pom
        return val.replaceAll( "'", "" ).replaceAll( "\"", "" ).replace( '\n', ' ' );
    }

    protected String createGemName( String groupId, String artifactId, String version )
    {
        // TODO: think about this
        return groupId + "." + artifactId;
    }

    protected String constructGemFileName( String gemName, String gemVersion, String platform )
    {
        StringBuilder sb = new StringBuilder();

        // gemspec.name - gemspec.version - gemspec.platform ".gem"
        sb.append( gemName ).append( "-" ).append( gemVersion );

        // only non Ruby platform should be appended
        // but we are doing "java" platform only, so this is wired in
        sb.append( "-" ).append( platform );

        // extension
        sb.append( ".gem" );

        return sb.toString();
    }

    protected String getGemFileName( String groupId, String artifactId, String version, String platform )
    {
        String gemName = createGemName( groupId, artifactId, version );

        String gemVersion = createGemVersion( version );

        return constructGemFileName( gemName, gemVersion, platform );
    }

    protected String getGemFileName( GemSpecification gemspec )
    {
        return constructGemFileName( gemspec.getName(), gemspec.getVersion().getVersion(), gemspec.getPlatform() );
    }

    protected String createGemVersion( String mavenVersion )
    {
        return maven2GemVersionConverter.createGemVersion( mavenVersion );
    }

    // ==

    private File generateRubyStub( GemSpecification gemspec, MavenArtifact artifact )
        throws IOException
    {
        String rubyStub = null;

        if ( artifact.getArtifactFile() != null )
        {
            rubyStub = IOUtil.toString( getClass().getResourceAsStream( "/metafile.rb.template" ) );
        }
        else
        {
            rubyStub = IOUtil.toString( getClass().getResourceAsStream( "/jarlessmetafile.rb.template" ) );
        }

        String[] titleParts = artifact.getPom().getArtifactId().split( "-" );
        StringBuilder titleizedArtifactId = new StringBuilder();
        for ( String part : titleParts )
        {
            if ( part != null && part.length() != 0 )
            {
                titleizedArtifactId.append( StringUtils.capitalise( part ) );
            }
        }

        String artifactName = "";

        if ( artifact.getArtifactFile() != null )
        {
            artifactName = artifact.getArtifactFile().getName();
        }

        rubyStub =
            rubyStub.replaceFirst( "\\$\\{version\\}", gemspec.getVersion().getVersion() ).replaceFirst(
                "\\$\\{maven_version\\}", artifact.getCoordinates().getVersion() ).replaceFirst( "\\$\\{jar_file\\}",
                artifactName ).replaceFirst( "\\$\\{titleized_classname\\}", titleizedArtifactId.toString() );

        File rubyStubFile = File.createTempFile( "rubyStub", ".rb.tmp" );

        FileWriter fw = new FileWriter( rubyStubFile );

        fw.write( rubyStub );

        fw.flush();

        fw.close();

        return rubyStubFile;
    }

    private GemDependency convertDependency( MavenArtifact artifact, Dependency dependency )
    {
        GemDependency result = new GemDependency();

        result.setName( createGemName( dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion() ) );

        result.setType( getRubyDependencyType( dependency.getScope() ) );

        GemRequirement requirement = new GemRequirement();

        // TODO: we are adding "hard" dependencies here, but we should maybe support Maven ranges too
        // based on http://blog.zenspider.com/2008/10/rubygems-howto-preventing-cata.html
        requirement.addRequirement( "~>", new GemVersion(
            createGemVersion( getDependencyVersion( artifact, dependency ) ) ) );

        result.setVersion_requirement( requirement );

        return result;
    }

    private String getRubyDependencyType( String dependencyScope )
    {
        // ruby scopes
        // :development
        // :runtime
        if ( "provided".equals( dependencyScope ) || "test".equals( dependencyScope ) )
        {
            return ":development";
        }
        else if ( "compile".equals( dependencyScope ) || "runtime".equals( dependencyScope ) )
        {
            return ":runtime";
        }
        else
        // dependencyScope: "system"
        {
            //TODO better throw an exception since there will be no gem for such a dependency or something else
            return ":runtime";
        }

    }

    private String getDependencyVersion( MavenArtifact artifact, Dependency dependency )
    {
        if ( dependency.getVersion() != null )
        {
            return dependency.getVersion();
        }
        else if ( StringUtils.equals( artifact.getCoordinates().getGroupId(), dependency.getGroupId() ) )
        {
            // hm, this is same groupId, let's suppose they have same dependency!
            return artifact.getCoordinates().getVersion();
        }
        else
        {
            // no help here, just interpolated POM
            return "unknown";
        }
    }
}
