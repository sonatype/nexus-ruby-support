package org.sonatype.nexus.ruby;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface RubygemsGateway {

    InputStream createGemspecRz( String gemname, InputStream gem ) throws IOException;

    InputStream emptyIndex();

    Object spec( InputStream gem );
    
    String pom( InputStream specRz );

    InputStream addSpec( Object spec, InputStream specsDump, SpecsIndexType type );

    InputStream deleteSpec( Object spec, InputStream specsDump );

    InputStream mergeSpecs( InputStream specs, List<InputStream> streams );

    List<String> listVersions( String name, InputStream inputStream, long modified );

    BundlerDependencies newBundlerDependencies( InputStream specs, long modified,
            InputStream prereleasedSpecs, long prereleasedModified,
            File cacheDir );

}
