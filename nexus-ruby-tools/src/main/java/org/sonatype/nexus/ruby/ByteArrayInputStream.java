package org.sonatype.nexus.ruby;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

class ByteArrayInputStream extends InputStream {

    private List<Long> bytes;
    private int cursor = 0;
    public ByteArrayInputStream(List<Long> bytes)
    {
        this.bytes = bytes;
    }
    
    @Override
    public int available() throws IOException {
        return bytes.size() - cursor;
    }
    
    @Override
    public void reset() throws IOException {
        cursor = 0;
    }

    @Override
    public int read() throws IOException {
        if (cursor < bytes.size()) 
        {
            return bytes.get( cursor ++ ).intValue();
        }
        else 
        {
            return -1;
        }
    }
}