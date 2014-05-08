package org.sonatype.nexus.ruby.cuba;

import java.io.InputStream;

import org.sonatype.nexus.ruby.Layout;
import org.sonatype.nexus.ruby.RubygemsFile;

public class RubygemsFileSystem
{
    private final Cuba cuba;
    
    private final Layout fileLayout;
    
    private final Layout getLayout;

    private final Layout postLayout;

    private final Layout deleteLayout;
    
    protected RubygemsFileSystem( Layout layout, Cuba cuba ){
        this( layout, layout, layout, layout, cuba );
    }

    protected RubygemsFileSystem( Layout fileLayout, Layout getLayout, Layout postLayout, Layout deleteLayout, Cuba cuba ){
        this.cuba = cuba;
        this.fileLayout = fileLayout;
        this.getLayout = getLayout;
        this.postLayout = postLayout;
        this.deleteLayout = deleteLayout;
    }

    public RubygemsFile file( String path )
    {
        return visit( fileLayout, path, "" );
    }

    public RubygemsFile file( String path, String query )
    {
        return visit( fileLayout, path, query );
    }

    public RubygemsFile get( String path )
    {
        return visit( getLayout, path, null );
    }

    public RubygemsFile get( String original, String query )
    {
        return visit( getLayout, original, query );
    }

    private RubygemsFile visit( Layout layout, String original, String query )
    {
        //normalize PATH-Separator from Windows platform to valid URL-Path
        //    https://github.com/sonatype/nexus-ruby-support/issues/38
        original = original.replace( '\\', '/' );
        if ( !original.startsWith( "/" ) )
        {
            original = "/" + original;
        }
        String path = original;
        if ( query == null )
        { 
            if ( original.contains( "?" ) )
            {
                int index = original.indexOf( "?" );
                if ( index > -1 )
                {
                    query = original.substring( index + 1 );
                    path = original.substring( 0, index );
                }
            }
            else
            {
                query = "";
            }
        }
                
        return new State( new Context( layout, original, query ), path, null ).nested( cuba );
    }

    public RubygemsFile post( InputStream is, String path )
    {
        if ( postLayout != null )
        {
            RubygemsFile file = visit( postLayout, path, "" );
            post( is, file );
            return file;
        }
        return null;
    }

    public void post( InputStream is, RubygemsFile file )
    {
        postLayout.addGem( is, file );
    }

    public RubygemsFile delete( String original )
    {
        return visit( deleteLayout, original, "" );
    }

    public String toString()
    {
        StringBuilder b = new StringBuilder( getClass().getSimpleName() );
        b.append( "<").append( cuba.getClass().getSimpleName() ).append( ">" );
        return b.toString();
    }
}