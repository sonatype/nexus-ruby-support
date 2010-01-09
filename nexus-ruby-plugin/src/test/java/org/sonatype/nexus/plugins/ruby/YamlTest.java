package org.sonatype.nexus.plugins.ruby;

import java.io.File;

import junit.framework.TestCase;

import org.jruby.embed.EmbedEvalUnit;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.LocalVariableBehavior;
import org.jruby.embed.PathType;
import org.jruby.embed.ScriptingContainer;

public class YamlTest
    extends TestCase
{
    private ScriptingContainer scriptingContainer;

    private EmbedEvalUnit generateIndexes;

    private EmbedEvalUnit generateLazyIndexes;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        scriptingContainer = new ScriptingContainer( LocalContextScope.SINGLETON, LocalVariableBehavior.PERSISTENT );

        generateIndexes = scriptingContainer.parse( PathType.CLASSPATH, "ruby-snippets/generate_indexes.rb" );

        generateLazyIndexes = scriptingContainer.parse( PathType.CLASSPATH, "ruby-snippets/generate_lazy_indexes.rb" );
    }

    public void gemGenerateIndexes( File basedir )
    {
        scriptingContainer.put( "@basedir", basedir.getAbsolutePath() );
        Object ret = generateIndexes.run();
        System.out.println( ret );
        scriptingContainer.getVarMap().clear();
    }

    public void gemGenerateLazyIndexes( File basedir )
    {
        scriptingContainer.put( "@basedir", basedir.getAbsolutePath() );
        Object ret = generateLazyIndexes.run();
        System.out.println( ret );
        scriptingContainer.getVarMap().clear();
    }

    /**
     * Just to leave this class alone.
     */
    public void testDummy()
    {
        assertTrue( true );
    }

    /**
     * Just for quick finding the bugge one
     */
    public void NOtestLazy()
    {
        gemGenerateLazyIndexes( new File( "/Users/cstamas/tmp/gems.sonatype.org" ) );
    }
}
