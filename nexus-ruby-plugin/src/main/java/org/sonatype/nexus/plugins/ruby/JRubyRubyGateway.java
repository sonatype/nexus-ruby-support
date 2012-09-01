package org.sonatype.nexus.plugins.ruby;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.LocalVariableBehavior;
import org.jruby.runtime.builtin.IRubyObject;
import org.sonatype.nexus.plugins.ruby.fs.SpecsIndexType;

@Component( role = RubyGateway.class )
public class JRubyRubyGateway
    implements RubyGateway
{

    @Requirement
    private Logger logger;

  //  @Requirement
    //ApplicationConfiguration configuration;

    private NexusScriptingContainer scriptingContainer;

    private final IRubyObject nexusRubygemsClass;

    protected Logger getLogger()
    {
        return logger;
    }
    
    public JRubyRubyGateway()
    {
        scriptingContainer = new NexusScriptingContainer( LocalContextScope.SINGLETON, LocalVariableBehavior.PERSISTENT );

        try
        {
        
            nexusRubygemsClass = scriptingContainer.parseFile( "nexus/rubygems.rb" ).run();

        }
        catch ( FileNotFoundException e )
        {
            throw new RuntimeException( e );
        }
    }
    
    private Object rubygems()
    {
        return scriptingContainer.callMethod(nexusRubygemsClass, "new", Object.class);
    }
    
    @Override
    public InputStream createGemspecRz( InputStream gem ) throws IOException
    {
        @SuppressWarnings( "unchecked" )
        List<Long> array = (List<Long>) scriptingContainer.callMethod( rubygems(), 
                "create_quick", 
                gem, 
                List.class );
        
        return new ByteArrayInputStream( array );
    }

    @Override
    public InputStream emptyIndex()
    {
        @SuppressWarnings( "unchecked" )
        List<Long> array = (List<Long>) scriptingContainer.callMethod( rubygems(),
                "empty_specs", 
                List.class );
        
        return new ByteArrayInputStream( array );
    }
    
    @Override
    public Object spec( InputStream gem ) {
        return scriptingContainer.callMethod( rubygems(), 
                "spec_get",
                gem,
                Object.class );
    }

    @Override
    public InputStream addSpec( Object spec, InputStream specsIndex, SpecsIndexType type ) {
        @SuppressWarnings( "unchecked" )
        List<Long> array = (List<Long>) scriptingContainer.callMethod( rubygems(),
                "add_spec", 
                new Object[] {
                    spec,
                    specsIndex,
                    type.name().toLowerCase()
                },
                List.class );
        
        return array == null ? null : new ByteArrayInputStream( array );
    }
    
    @Override
    public InputStream deleteSpec( Object spec, InputStream specsIndex ) {
        @SuppressWarnings( "unchecked" )
        List<Long> array = (List<Long>) scriptingContainer.callMethod( rubygems(),
                "delete_spec", 
                new Object[] {
                    spec,
                    specsIndex,
                },
                List.class );
        
        return array == null ? null : new ByteArrayInputStream( array );
    }

    @Override
    public InputStream mergeSpecs(InputStream specs,
            List<InputStream> streams) {
        @SuppressWarnings( "unchecked" )
        List<Long> array = (List<Long>) scriptingContainer.callMethod( rubygems(),
                "merge_specs", 
                new Object[] {
                    specs,
                    streams,
                },
                List.class );
        
        return array == null ? null : new ByteArrayInputStream( array );
    }    
}
