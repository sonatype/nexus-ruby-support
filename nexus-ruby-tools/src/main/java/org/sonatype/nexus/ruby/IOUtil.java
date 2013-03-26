package org.sonatype.nexus.ruby;

import java.io.IOException;
import java.io.InputStream;

class IOUtil{
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
}