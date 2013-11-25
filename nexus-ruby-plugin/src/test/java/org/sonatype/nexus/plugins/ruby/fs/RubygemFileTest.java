package org.sonatype.nexus.plugins.ruby.fs;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import junit.framework.TestCase;

import org.junit.Test;
import org.sonatype.nexus.plugins.ruby.fs.RubygemFile.Type;
import org.sonatype.nexus.proxy.maven.gav.Gav;

public class RubygemFileTest
    extends TestCase
{
    
    @Test
    public void testInstall()
        throws Exception
    {
        assertFilename( "_-1.0" );
        assertFilename( "--0.1.0" );
        assertFilename( "0xff-0.1" );
        assertFilename( "bla-0.1" );
        assertFilename( "BlaBla-1" );
        assertFilename( "thread_safe-1" );
    }
    
    public void testGAV()
    {
        RubygemFile file = new RubygemFile( new Gav( "rubygems", "aid", "123" ) );
        assertThat( file.getGemnameWithVersion(), is( "aid-123-java" ) );
        file = new RubygemFile( new Gav( "rubygems", "aid", "123-SNAPSHOT" ) );
        assertThat( file.getGemnameWithVersion(), is( "aid-123-java" ) );
    }

    private void assertFilename( String filename )
    {
        assertGemspecRz( filename );
        assertGem( filename );
    }
    
    private void assertGemspecRz( String filename )
    {
        assertGemspecRz( filename, "/" );
        assertGemspecRz( filename, "\\" );
    }
    
    private void assertGemspecRz( String filename, String pathSeparator )
    {
        RubygemFile gem = RubygemFile.fromFilename( pathSeparator + filename + ".gemspec.rz" );
        assertThat( gem.getGemnameWithVersion(), is( filename ) );
        assertThat( gem.getGemspecRz(), is( "/quick/Marshal.4.8/" + filename.charAt( 0 ) + "/" + filename + ".gemspec.rz" ) );
        assertThat( gem.getType(), is( Type.GEMSPEC ) );
        assertThat( gem.getMime(), is( "application/x-ruby-marshal" ) );
        assertThat( gem.getPath(), is( "/" + filename.charAt( 0 ) + "/" + filename + ".gemspec.rz" ) );
        assertTrue( RubygemFile.isGemspec( gem.getPath() ) );
    }
    
    private void assertGem( String filename )
    {
        assertGem( filename, "/" );
        assertGem( filename, "\\" );
    }
    
    private void assertGem( String filename, String pathSeparator )
    {
        RubygemFile gem = RubygemFile.fromFilename( pathSeparator + "gems" + pathSeparator + filename + ".gem" );
        assertThat( gem.getGemnameWithVersion(), is( filename ) );
        assertThat( gem.getGemspecRz(), is( "/quick/Marshal.4.8/" + filename.charAt( 0 ) + "/" + filename + ".gemspec.rz" ) );
        assertThat( gem.getType(), is( Type.GEM ) );
        assertThat( gem.getMime(), is( "application/x-rubygems" ) );
        assertThat( gem.getPath(), is( "/gems/" + filename.charAt( 0 ) + "/" + filename + ".gem" ) );
        assertTrue( RubygemFile.isGem( gem.getPath() ) );
    }

}
