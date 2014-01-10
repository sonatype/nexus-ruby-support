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

    private final JRubyScriptingContainer scriptingContainer;

    private final IRubyObject nexusRubygemsClass;

    private Object rubygems;
    
    public DefaultRubygemsGateway()
    {
        scriptingContainer = new JRubyScriptingContainer( LocalContextScope.THREADSAFE, 
                                                          LocalVariableBehavior.PERSISTENT );

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

    private <T> T callMethod( String methodName, Object singleArg, Class<T> returnType ) {
        return scriptingContainer.callMethod( rubygems(), methodName, singleArg, returnType );
    }

    private <T> T callMethod( String methodName, Object[] args, Class<T> returnType ) {
        return scriptingContainer.callMethod( rubygems(), methodName, args, returnType );
    }

    private <T> T callMethod( String methodName, Class<T> returnType ) {
        return scriptingContainer.callMethod( rubygems(), methodName, returnType );
    }
    
    @Override
    public ByteArrayInputStream createGemspecRz( String gemname, InputStream gem )
    {
        try
        {
            @SuppressWarnings( "unchecked" )
            List<Long> array = (List<Long>) callMethod( "create_quick",
                                                        new Object[] { gemname, gem },
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
        List<Long> array = (List<Long>) callMethod( "empty_specs", List.class );
        
        return new ByteArrayInputStream( array );
    }
    
    @Override
    public Object spec( InputStream gem ) {
        try
        {
            return callMethod( "spec_get", gem, Object.class );
        }
        finally
        {
            IOUtil.close( gem );
        }
    }

    @SuppressWarnings("resource")
    @Override
    public InputStream addSpec( Object spec, InputStream specsIndex, SpecsIndexType type ) {
        try
        {
            @SuppressWarnings( "unchecked" )
            List<Long> array = (List<Long>) callMethod( "add_spec", 
                                                        new Object[] { spec,
                                                                       specsIndex,
                                                                       type.name().toLowerCase() },
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
	return deleteSpec( spec, specsIndex, null );
    }
    
    @SuppressWarnings("resource")
    @Override
    public InputStream deleteSpec( Object spec, InputStream specsIndex, InputStream refSpecs ) {
        try
        {
            @SuppressWarnings( "unchecked" )
            List<Long> array = (List<Long>) callMethod( "delete_spec",
                                                        new Object[] { spec,
                                                                       specsIndex,
                                                                       refSpecs },
                                                        List.class );
        
            return array == null ? null : new ByteArrayInputStream( array );
        }
        finally
        {
            IOUtil.close( specsIndex );
        }
    }

    @SuppressWarnings("resource")
    @Override
    public InputStream mergeSpecs( InputStream specs,
            List<InputStream> streams, boolean latest ) {
        try
        {
            @SuppressWarnings( "unchecked" )
            List<Long> array = (List<Long>) callMethod( "merge_specs",
                                                        new Object[] { specs,
                                                                       streams,
                                                                       latest },
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
            return callMethod( "to_pom", specRz, String.class );
        }
        finally
        {
            IOUtil.close( specRz );
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized List<String> listVersions(String name, 
                                                  InputStream inputStream, 
                                                  long modified, 
                                                  boolean prerelease )
    {
        try
        {
            return (List<String>) callMethod( "list_versions",
                                              new Object[] { name,
                                                             inputStream,
                                                             modified ,
                                                             prerelease },
                                              List.class );
        }
        finally
        {
            IOUtil.close( inputStream );
        }
    }

    @Override
    public synchronized BundlerDependencies newBundlerDependencies()
    {
        Object bundlerDeps = callMethod( "dependencies",
                                         new Object[] { null, 0, null, 0 },
                                         Object.class );

        return new BundlerDependencies( scriptingContainer, bundlerDeps );
    }

    @Override
    public synchronized BundlerDependencies newBundlerDependencies( InputStream specs, long modified,
            InputStream prereleasedSpecs, long prereleasedModified )
    {
        try
        {
            Object bundlerDeps = callMethod( "dependencies",
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

    @Override
    public void recreateRubygemsIndex( String directory )
    {
        callMethod( "recreate_rubygems_index", directory, Void.class );
    }

    @Override
    public void purgeBrokenDepencencyFiles( String directory )
    {
        callMethod( "purge_broken_depencency_files", directory, Void.class );        
    }

    @Override
    public void purgeBrokenGemspecFiles( String directory )
    {
        callMethod( "purge_broken_gemspec_files", directory, Void.class );        
    }

    @Override
    public ByteArrayInputStream createGemspecRz( Object spec )
    {
        @SuppressWarnings( "unchecked" )
        List<Long> array = (List<Long>) callMethod( "create_quick",
                                                    new Object[] { spec },
                                                    List.class );
        
        return new ByteArrayInputStream( array );
    }

    @Override
    public String gemname( Object spec )
    {
        return scriptingContainer.callMethod( spec, "file_name", String.class );
    }
}
