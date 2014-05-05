package org.sonatype.nexus.ruby.cuba;

import org.sonatype.nexus.ruby.Layout;
import org.sonatype.nexus.ruby.RubygemsFile;

public class Bootstrap
{
    private final Cuba cuba;
    
    public final Layout layout;
    
    protected Bootstrap( Layout layout, Cuba cuba ){
        this.cuba = cuba;
        this.layout = layout;
    }

    public RubygemsFile accept( String original )
    {
        return accept( original, null );
    }

    public RubygemsFile accept( String original, String query )
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
                
        RubygemsFile result = new State( new Context( layout, original, query ), path, null ).nested( cuba );
        if ( result != null && ! result.exists() )
        {
            return layout.notFound( path );
        }
        return result;
    }   
    
    public String toString()
    {
        StringBuilder b = new StringBuilder( getClass().getSimpleName() );
        b.append( "<").append( cuba.getClass().getSimpleName() ).append( ">" );
        return b.toString();
    }
}