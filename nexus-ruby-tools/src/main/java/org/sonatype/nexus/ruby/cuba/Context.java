package org.sonatype.nexus.ruby.cuba;

import org.sonatype.nexus.ruby.Layout;



public class Context
{
   
    public final String original;
    
    public final String query;
    
    public final Layout layout;
        
    public Context( Layout layout, String original, String query ){
        this.original = original;
        this.query = query;
        this.layout = layout;
    }
 
    public String toString()
    {
        StringBuilder b = new StringBuilder( getClass().getSimpleName() );
        b.append( "<").append( original );
        if ( !query.isEmpty() )
        {
            b.append(  "?" ).append( query );
        }
        b.append( ">" );
        return b.toString();
    }
}