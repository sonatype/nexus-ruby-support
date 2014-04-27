package org.sonatype.nexus.ruby.cuba;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sonatype.nexus.ruby.RubygemsFile;



public class State
{
    static Pattern ROOT = Pattern.compile( "^/([^/]*).*" );
    
    public final String path;
    
    public final String part;
    
    public final Context context;
        
    public State( Context root, String path, String part ){
        this.context = root;
        this.path = path;
        this.part = part;
    }
        
    public RubygemsFile nested( Cuba cuba )
    {
        if ( path.isEmpty() )
        {
            return cuba.on( new State( context, "", "" ) );
        }
        Matcher m = ROOT.matcher( path );
        
        if ( m.matches() )
        {
            String name = m.group( 1 );
            return cuba.on( new State( context, 
                                       this.path.substring( 1 + name.length() ), 
                                       name ) );
        }
        return context.layout.notFound( context.original );
    }
    
    public String toString()
    {
        StringBuilder b = new StringBuilder( getClass().getSimpleName() );
        b.append( "<" ).append( path ).append( "," ).append( part ).append( "> )" );
        return b.toString();
    }
}