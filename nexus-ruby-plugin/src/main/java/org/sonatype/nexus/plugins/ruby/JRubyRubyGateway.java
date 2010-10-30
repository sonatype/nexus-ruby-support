package org.sonatype.nexus.plugins.ruby;

import java.io.File;

import org.codehaus.plexus.component.annotations.Component;
import org.jruby.embed.EmbedEvalUnit;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.LocalVariableBehavior;
import org.jruby.embed.PathType;
import org.jruby.embed.ScriptingContainer;

@Component( role = RubyGateway.class )
public class JRubyRubyGateway
    extends DefaultRubyGateway
    implements RubyGateway
{
    private ScriptingContainer scriptingContainer;

    private EmbedEvalUnit generateIndexes;

    private EmbedEvalUnit generateLazyIndexes;

    public JRubyRubyGateway()
    {
        ClassLoader oldTCCL = Thread.currentThread().getContextClassLoader();

        try
        {
            Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );

            scriptingContainer = new ScriptingContainer( LocalContextScope.SINGLETON, LocalVariableBehavior.PERSISTENT );

            generateIndexes = scriptingContainer.parse( PathType.CLASSPATH, "ruby-snippets/generate_indexes.rb" );

            generateLazyIndexes =
                scriptingContainer.parse( PathType.CLASSPATH, "ruby-snippets/generate_lazy_indexes.rb" );
        }
        finally
        {
            Thread.currentThread().setContextClassLoader( oldTCCL );
        }
    }

    @Override
    public synchronized void gemGenerateIndexes( File basedir, boolean update )
    {
        // work around on ubuntu systems since jruby can not delete the directory
        basedir.delete();

        getLogger().info(
            "Invoking Gem::Indexer for " + ( update ? "update" : "generate" ) + " on basedir \""
                + basedir.getAbsolutePath() + "\"..." );
        scriptingContainer.put( "@basedir", basedir.getAbsolutePath() );
        scriptingContainer.put( "@update", update );
        Object ret = generateIndexes.run();
        System.out.println( ret );
        scriptingContainer.getVarMap().clear();
        getLogger().info(
            "Invoking Gem::Indexer for " + ( update ? "update" : "generate" ) + " on basedir \""
                + basedir.getAbsolutePath() + "\"... DONE" );
    }

    @Override
    public synchronized void gemGenerateLazyIndexes( File basedir, boolean update )
    {
        getLogger().info(
            "Invoking Gem::NexusIndexer for " + ( update ? "update" : "generate" ) + " on basedir \""
                + basedir.getAbsolutePath() + "\"..." );
        scriptingContainer.put( "@basedir", basedir.getAbsolutePath() );
        scriptingContainer.put( "@update", update );
        Object ret = generateLazyIndexes.run();
        System.out.println( ret );
        scriptingContainer.getVarMap().clear();
        getLogger().info(
            "Invoking Gem::NexusIndexer for " + ( update ? "update" : "generate" ) + " on basedir \""
                + basedir.getAbsolutePath() + "\"... DONE" );
    }
}
