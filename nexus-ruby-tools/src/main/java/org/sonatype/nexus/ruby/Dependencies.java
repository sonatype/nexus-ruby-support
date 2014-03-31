package org.sonatype.nexus.ruby;

import org.jruby.embed.ScriptingContainer;


public class Dependencies extends ScriptWrapper
{   
    public Dependencies( ScriptingContainer scriptingContainer,
                         Object dependencies )
    {
        super( scriptingContainer, dependencies );
    }

    public String[] javaVersions( boolean prereleased )
    {
        return callMethod( "java_versions", prereleased, String[].class );
    }
    
    public String platform( String version )
    {
        return callMethod( "platform", String.class );
    }

    public String name()
    {
        return callMethod( "name", String.class );
    }
}