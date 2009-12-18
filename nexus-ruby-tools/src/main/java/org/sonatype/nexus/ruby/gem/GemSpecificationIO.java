package org.sonatype.nexus.ruby.gem;

import java.io.IOException;

public interface GemSpecificationIO
{
    GemSpecification read( String string )
        throws IOException;

    String write( GemSpecification gemspec )
        throws IOException;
}
