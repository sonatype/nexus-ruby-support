package org.sonatype.nexus.ruby;

import java.io.FileNotFoundException;
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
    public InputStream createGemspecRz( String gemname, InputStream gem )
    {
        try
        {
            @SuppressWarnings( "unchecked" )
            List<Long> array = (List<Long>) scriptingContainer.callMethod( rubygems(), 
                "create_quick", 
                new Object[] {
                    gemname,
                    gem 
                },
                List.class );
        
            return new ByteArrayInputStream( array );
        }
        finally
        {
            IOUtil.close( gem );
        }
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
        try
        {
            return scriptingContainer.callMethod( rubygems(), 
                "spec_get",
                gem,
                Object.class );
        }
        finally
        {
            IOUtil.close( gem );
        }
    }

    @Override
    public InputStream addSpec( Object spec, InputStream specsIndex, SpecsIndexType type ) {
        try
        {
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
        finally
        {
            IOUtil.close( specsIndex );
        }
    }
    
    @Override
    public InputStream deleteSpec( Object spec, InputStream specsIndex ) {
        try
        {
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
        finally
        {
            IOUtil.close( specsIndex );
        }
    }

    @Override
    public InputStream mergeSpecs(InputStream specs,
            List<InputStream> streams) {
        try
        {
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
        finally
        {
            IOUtil.close( specs );
            for( InputStream in: streams )
            {
                IOUtil.close( in );
            }
        }
    }

    @Override
    public String pom(InputStream specRz)
    {
        try
        {
            return scriptingContainer.callMethod( rubygems(), 
                "to_pom",
                specRz,
                String.class );
        }
        finally
        {
            IOUtil.close( specRz );
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized List<String> listVersions(String name, InputStream inputStream, long modified )
    {
        try
        {
            return (List<String>) scriptingContainer.callMethod( rubygems(), 
                "list_versions",
                new Object[] { name,
                               inputStream, 
                               modified },
                List.class );
        }
        finally
        {
            IOUtil.close( inputStream );
        }
    }

    @Override
    public BundlerDependencies newBundlerDependencies()
    {
        Object bundlerDeps = scriptingContainer.callMethod( rubygems(),
            "dependencies", 
            new Object[] { null, 0, null, 0 },
            Object.class );

        return new BundlerDependencies(scriptingContainer, bundlerDeps);
    }

    @Override
    public BundlerDependencies newBundlerDependencies( InputStream specs, long modified,
            InputStream prereleasedSpecs, long prereleasedModified )
    {
        try
        {
            Object bundlerDeps = scriptingContainer.callMethod( rubygems(),
                    "dependencies", 
                    new Object[] { specs, modified, prereleasedSpecs, prereleasedModified },
                    Object.class );

            return new BundlerDependencies(scriptingContainer, bundlerDeps);
        }
        finally
        {
            IOUtil.close( specs );
            IOUtil.close( prereleasedSpecs );
        }
    }
}
