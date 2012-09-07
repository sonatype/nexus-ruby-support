package org.sonatype.nexus.ruby;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface RubygemsGateway {

    InputStream createGemspecRz( InputStream pathToGem ) throws IOException;

    InputStream emptyIndex();

    Object spec( InputStream gem );

    InputStream addSpec( Object spec, InputStream specsDump, SpecsIndexType type );

    InputStream deleteSpec( Object spec, InputStream specsDump );

    InputStream mergeSpecs( InputStream specs, List<InputStream> streams );

}