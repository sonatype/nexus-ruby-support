package org.sonatype.nexus.plugins.ruby;

import java.io.File;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.jruby.embed.EmbedEvalUnit;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.LocalVariableBehavior;
import org.jruby.embed.ScriptingContainer;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;

@Component( role = RubyGateway.class )
public class JRubyRubyGateway
    extends DefaultRubyGateway
    implements RubyGateway
{

    @Requirement
    ApplicationConfiguration configuration;

    private ScriptingContainer scriptingContainer;

    private final EmbedEvalUnit generateIndexes;

    public JRubyRubyGateway()
    {
        scriptingContainer = new ScriptingContainer( LocalContextScope.SINGLETON, LocalVariableBehavior.PERSISTENT );
        scriptingContainer.setClassLoader(JRubyRubyGateway.class.getClassLoader());

        // The JRuby and all the scripts is in this plugin's CL!
        scriptingContainer.getProvider().getRubyInstanceConfig().setJRubyHome(
            JRubyRubyGateway.class.getClassLoader().getResource( "META-INF/jruby.home" ).toString().replaceFirst(
                "^jar:", "" ) );

        generateIndexes =
            scriptingContainer.parse(
                    JRubyRubyGateway.class.getClassLoader().getResourceAsStream( "ruby-snippets/generate_indexes.rb" ),
                "generate_indexes.rb" );
    }

    @Override
    public synchronized void gemGenerateIndexes( File basedir, boolean update )
    {
        // work around on ubuntu systems since jruby can not delete the directory
        // TODO why ???
        basedir.delete();

        getLogger().info(
            "Invoking Gem::Indexer for " + ( update ? "update" : "generate" ) + " on basedir \""
                + basedir.getAbsolutePath() + "\"..." );
        scriptingContainer.put( "@basedir", basedir.getAbsolutePath() );
        scriptingContainer.put( "@tempdir", configuration.getTemporaryDirectory().getAbsolutePath() );
        scriptingContainer.put( "@update", update );
        generateIndexes.run();
        scriptingContainer.getVarMap().clear();
        getLogger().info(
            "Invoking Gem::Indexer for " + ( update ? "update" : "generate" ) + " on basedir \""
                + basedir.getAbsolutePath() + "\"... DONE" );
    }
}
