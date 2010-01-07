package org.sonatype.nexus.ruby;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Developer;
import org.apache.maven.model.License;
import org.apache.maven.model.Model;
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
    @Requirement
    private GemPackager gemPackager;

    private Maven2GemVersionConverter maven2GemVersionConverter = new Maven2GemVersionConverter();

    public String createGemName( String groupId, String artifactId, String version )
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

    public String getGemFileName( String groupId, String artifactId, String version, String platform )
    {
        String gemName = createGemName( groupId, artifactId, version );

        String gemVersion = createGemVersion( version );

        return constructGemFileName( gemName, gemVersion, platform );
    }

    public String getGemFileName( Model pom )
    {
        return getGemFileName( getGroupId( pom ), pom.getArtifactId(), getVersion( pom ), PLATFORM_JAVA );
    }

    public String getGemFileName( GemSpecification gemspec )
    {
        return constructGemFileName( gemspec.getName(), gemspec.getVersion().getVersion(), gemspec.getPlatform() );
    }

    public String createGemVersion( String mavenVersion )
    {
        return maven2GemVersionConverter.createGemVersion( mavenVersion );
    }

    public GemSpecification createSpecification( Model pom )
    {
        GemSpecification result = new GemSpecification();

        // this is fix
        result.setPlatform( "java" );

        // the must ones
        result.setName( createGemName( getGroupId( pom ), pom.getArtifactId(), getVersion( pom ) ) );
        result.setVersion( new GemVersion( createGemVersion( getVersion( pom ) ) ) );

        // dependencies
        if ( pom.getDependencies().size() > 0 )
        {
            for ( Dependency dependency : pom.getDependencies() )
            {
                result.getDependencies().add( convertDependency( pom, dependency ) );
            }
        }

        // and other stuff "nice to have"
        result.setDate( new Date() ); // now
        result.setDescription( pom.getDescription() != null ? pom.getDescription() : pom.getName() );
        result.setSummary( pom.getName() );
        result.setHomepage( pom.getUrl() );

        if ( pom.getLicenses().size() > 0 )
        {
            for ( License license : pom.getLicenses() )
            {
                result.getLicenses().add( license.getName() + " (" + license.getUrl() + ")" );
            }
        }
        if ( pom.getDevelopers().size() > 0 )
        {
            for ( Developer developer : pom.getDevelopers() )
            {
                result.getAuthors().add( developer.getName() + " (" + developer.getEmail() + ")" );
            }
        }

        // by default, we pack into lib/ inside gem (where is the jar and the stub ruby)
        result.getRequire_paths().add( "lib" );
        return result;
    }

    public GemArtifact createGemFromArtifact( MavenArtifact artifact, File target )
        throws IOException
    {
        GemSpecification gemspec = createSpecification( artifact.getPom() );

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
                "\\$\\{maven_version\\}", getVersion( artifact.getPom() ) ).replaceFirst( "\\$\\{jar_file\\}",
                artifactName ).replaceFirst( "\\$\\{titleized_classname\\}", titleizedArtifactId.toString() );

        File rubyStubFile = File.createTempFile( "rubyStub", ".rb.tmp" );

        FileWriter fw = new FileWriter( rubyStubFile );

        fw.write( rubyStub );

        fw.flush();

        fw.close();

        return rubyStubFile;
    }

    private GemDependency convertDependency( Model pom, Dependency dependency )
    {
        GemDependency result = new GemDependency();

        result.setName( createGemName( dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion() ) );

        result.setType( getRubyDependencyType( dependency.getScope() ) );

        GemRequirement requirement = new GemRequirement();

        // TODO: we are adding "hard" dependencies here, but we should maybe support Maven ranges too
        // based on http://blog.zenspider.com/2008/10/rubygems-howto-preventing-cata.html
        requirement
            .addRequirement( "~>", new GemVersion( createGemVersion( getDependencyVersion( pom, dependency ) ) ) );

        result.setVersion_requirement( requirement );

        return result;
    }

    private String getRubyDependencyType( String dependencyScope )
    {
        // ruby scopes
        // :development
        // :runtime
        return ":runtime";
    }

    private String getGroupId( Model pom )
    {
        return pom.getGroupId() != null ? pom.getGroupId() : pom.getParent().getGroupId();
    }

    private String getVersion( Model pom )
    {
        return pom.getVersion() != null ? pom.getVersion() : pom.getParent().getVersion();
    }

    private String getDependencyVersion( Model pom, Dependency dependency )
    {
        if ( dependency.getVersion() != null )
        {
            return dependency.getVersion();
        }
        else if ( getGroupId( pom ).equals( dependency.getGroupId() ) )
        {
            // hm, this is same groupId, let's suppose they have same dependency!
            return getVersion( pom );
        }
        else
        {
            // no help here, just interpolated POM
            return "unknown";
        }
    }
}
