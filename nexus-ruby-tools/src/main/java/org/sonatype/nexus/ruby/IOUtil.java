package org.sonatype.nexus.ruby;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtil  {
    public static void close( InputStream inputStream )
    {
        if ( inputStream == null )
        {
            return;
        }
    
        try
        {
            inputStream.close();
        }
        catch( IOException ex )
        {
            // ignore
        }
    }
    /**
     * Copy bytes from an <code>InputStream</code> to an <code>OutputStream</code>.
     * @param bufferSize Size of internal buffer to use.
     */
    public static void copy( final InputStream input,
                             final OutputStream output )
        throws IOException
    {
        final byte[] buffer = new byte[4096];
        int n = 0;
        while ( -1 != ( n = input.read( buffer ) ) )
        {
            output.write( buffer, 0, n );
        }
    }
}