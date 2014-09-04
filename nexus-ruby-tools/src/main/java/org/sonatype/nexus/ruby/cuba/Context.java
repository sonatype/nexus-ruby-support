package org.sonatype.nexus.ruby.cuba;

import org.sonatype.nexus.ruby.RubygemsFileFactory;

/**
 * the <code>Context</code> carries the original path and the query string
 * from the (HTTP) request as well the <code>RubygemsFileFactory</code> which
 * is used by the <code>Cuba</code> objects to create <code>RubygemsFile</code>s.
 * 
 * it is basically the static part of the <code>State</code> object and is immutable.
 * 
 * @author christian
 *
 */

public class Context
{
   
    public final String original;
    
    public final String query;
    
    public final RubygemsFileFactory factory;
        
    public Context( RubygemsFileFactory factory, String original, String query ){
        this.original = original;
        this.query = query;
        this.factory = factory;
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