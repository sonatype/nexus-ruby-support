package org.sonatype.nexus.ruby;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.jruby.embed.LocalContextScope;
import org.jruby.embed.LocalVariableBehavior;
import org.jruby.runtime.builtin.IRubyObject;

public class DefaultRubygemsGateway
    implements RubygemsGateway
{

    private JRubyScriptingContainer scriptingContainer;

    private final IRubyObject nexusRubygemsClass;

    private Object rubygems;
    
    public DefaultRubygemsGateway()
    {
        scriptingContainer = new JRubyScriptingContainer( LocalContextScope.THREADSAFE, LocalVariableBehavior.PERSISTENT );

        try
        {
        
            nexusRubygemsClass = scriptingContainer.parseFile( "nexus/rubygems.rb" ).run();

        }
        catch ( FileNotFoundException e )
        {
            throw new RuntimeException( e );
        }
    }
    
    private synchronized Object rubygems()
    {
        if (rubygems == null )
        { 
            rubygems = scriptingContainer.callMethod( nexusRubygemsClass, "new", Object.class );
        }
        return rubygems;
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

    @Override
    public String pom(InputStream specRz) {
        return scriptingContainer.callMethod( rubygems(), 
                "to_pom",
                specRz,
                String.class );
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> listVersions(String name, InputStream inputStream, long modified ) {
        return (List<String>) scriptingContainer.callMethod( rubygems(), 
                "list_versions",
                new Object[] { name,
                               inputStream, 
                               modified },
                List.class );
    }
}
