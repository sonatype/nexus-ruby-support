package org.sonatype.nexus.plugins.ruby;

import java.io.FileOutputStream;
import java.io.InputStream;

import org.codehaus.plexus.PlexusTestCase;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.LocalVariableBehavior;
import org.jruby.runtime.builtin.IRubyObject;
import org.junit.Before;
import org.junit.Test;

public class JRubyRubyGatewayTest
    extends PlexusTestCase
{
    
    private NexusScriptingContainer scriptingContainer;

    @Before
    public void setUp()
    {
        scriptingContainer = new NexusScriptingContainer( LocalContextScope.SINGLETON, LocalVariableBehavior.PERSISTENT );
    }
    
    @Test
    public void testGenerateGemspecRz()
        throws Exception
    {
        JRubyRubyGateway gateway = new JRubyRubyGateway();
        String gemPath = "src/test/repo/gems/n/nexus-0.1.0.gem";
        
        InputStream is = gateway.createGemspecRz( gemPath );
        int c = is.read();
        String gemspecPath = "target/nexus-0.1.0.gemspec.rz";
        FileOutputStream out = new FileOutputStream(gemspecPath);
        while( c != -1 )
        {
            out.write(c);
            c = is.read();
        }
        out.close();
        is.close();

        IRubyObject check = scriptingContainer.parseFile( "nexus/check_gemspec_rz.rb" ).run();
        boolean equalSpecs = scriptingContainer.callMethod( check, "check",
                    new Object[] { gemPath, gemspecPath }, 
                    Boolean.class );
        assertTrue("spec from stream equal spec from gem", equalSpecs);
    }
}
