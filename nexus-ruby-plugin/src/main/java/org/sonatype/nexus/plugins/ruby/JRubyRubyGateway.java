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

    public JRubyRubyGateway()
    {
        scriptingContainer = new ScriptingContainer( LocalContextScope.SINGLETON, LocalVariableBehavior.PERSISTENT );
        
        generateIndexes = scriptingContainer.parse( PathType.CLASSPATH, "ruby/generate_indexes.rb" );
    }

    @Override
    public synchronized void gemGenerateIndexes( File basedir )
    {
        scriptingContainer.put( "@basedir", basedir.getAbsolutePath() );
        Object ret = generateIndexes.run();
        System.out.println( ret );
        scriptingContainer.getVarMap().clear();
    }
}
