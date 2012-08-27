package org.sonatype.nexus.plugins.ruby;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.jruby.embed.EmbedEvalUnit;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.LocalVariableBehavior;
import org.jruby.runtime.builtin.IRubyObject;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.plugins.ruby.fs.SpecsIndexType;

@Component( role = RubyGateway.class )
public class JRubyRubyGateway
    extends DefaultRubyGateway
    implements RubyGateway
{

    @Requirement
    ApplicationConfiguration configuration;

    private NexusScriptingContainer scriptingContainer;

    private final EmbedEvalUnit generateIndexes;

    private final IRubyObject nexusRubygemsClass;

    public JRubyRubyGateway()
    {
        scriptingContainer = new NexusScriptingContainer( LocalContextScope.SINGLETON, LocalVariableBehavior.PERSISTENT );

        try
        {
            generateIndexes = scriptingContainer.parseFile( "ruby-snippets/generate_indexes.rb" );
        
            nexusRubygemsClass = scriptingContainer.parseFile( "nexus/rubygems.rb" ).run();

        }
        catch ( FileNotFoundException e )
        {
            throw new RuntimeException( e );
        }
    }

    @Override
    public synchronized void gemGenerateIndexes( File basedir, boolean update )
    {
        getLogger().info(
            "Invoking Gem::Indexer for " + ( update ? "update" : "generate" ) + " on basedir \""
                + basedir.getAbsolutePath() + "\"..." );
        scriptingContainer.put( "@basedir", basedir.getAbsolutePath() );
        scriptingContainer.put( "@tempdir", configuration.getTemporaryDirectory().getAbsolutePath() );
        scriptingContainer.put( "@update", false );// udpate); TODO update does not copy the quick files into place.
        generateIndexes.run();
        scriptingContainer.getVarMap().clear();
        getLogger().info(
            "Invoking Gem::Indexer for " + ( update ? "update" : "generate" ) + " on basedir \""
                + basedir.getAbsolutePath() + "\"... DONE" );
    }
    
    static class ByteArrayInputStream extends InputStream {

        private List<Long> bytes;
        private int cursor = 0;
        public ByteArrayInputStream(List<Long> bytes)
        {
            this.bytes = bytes;
        }
        
        @Override
        public int available() throws IOException {
            return bytes.size() - cursor;
        }
        
        @Override
        public void reset() throws IOException {
            cursor = 0;
        }

        @Override
        public int read() throws IOException {
            System.out.print('.');
            if (cursor < bytes.size()) 
            {
                return bytes.get( cursor ++ ).intValue();
            }
            else 
            {
                System.out.println();
                return -1;
            }
        }
    }
    
    private Object rubygems()
    {
        return scriptingContainer.callMethod(nexusRubygemsClass, "new", ".", Object.class);
    }
    
    public InputStream createGemspecRz( String pathToGem ) throws IOException
    {
        @SuppressWarnings( "unchecked" )
        List<Long> array = (List<Long>) scriptingContainer.callMethod( rubygems(), 
                "create_quick", 
                pathToGem, 
                List.class );
        
        return new ByteArrayInputStream( array );
    }

    public InputStream emptyIndex()
    {
        @SuppressWarnings( "unchecked" )
        List<Long> array = (List<Long>) scriptingContainer.callMethod( rubygems(),
                "empty_specs", 
                List.class );
        
        return new ByteArrayInputStream( array );
    }
    
    public Object spec( File gem ) {
        return scriptingContainer.callMethod( rubygems(), 
                "spec_get",
                gem.getAbsolutePath(),
                Object.class );
    }

    public InputStream addSpec( Object spec, File specsDump, SpecsIndexType type ) {
        @SuppressWarnings( "unchecked" )
        List<Long> array = (List<Long>) scriptingContainer.callMethod( rubygems(),
                "add_spec", 
                new Object[] {
                    spec,
                    specsDump.getAbsolutePath(),
                    type.name().toLowerCase()
                },
                List.class );
        
        return array == null ? null : new ByteArrayInputStream( array );
    }
    
    public InputStream deleteSpec( Object spec, File specsDump ) {
        @SuppressWarnings( "unchecked" )
        List<Long> array = (List<Long>) scriptingContainer.callMethod( rubygems(),
                "delete_spec", 
                new Object[] {
                    spec,
                    specsDump.getAbsolutePath(),
                },
                List.class );
        
        return array == null ? null : new ByteArrayInputStream( array );
    }
    
}
