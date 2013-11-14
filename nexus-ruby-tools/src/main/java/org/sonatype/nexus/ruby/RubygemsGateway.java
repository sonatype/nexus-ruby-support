package org.sonatype.nexus.ruby;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface RubygemsGateway {

    void recreateRubygemsIndex( String directory );

    void purgeBrokenDepencencyFiles( String directory );

    void purgeBrokenGemspecFiles( String directory );
    
    InputStream createGemspecRz( String gemname, InputStream gem ) throws IOException;

    InputStream emptyIndex();

    Object spec( InputStream gem );
    
    String pom( InputStream specRz );

    InputStream addSpec( Object spec, InputStream specsDump, SpecsIndexType type );

    InputStream deleteSpec( Object spec, InputStream specsDump );

    InputStream deleteSpec( Object spec, InputStream specsIndex, InputStream refSpecs );

    InputStream mergeSpecs( InputStream specs, List<InputStream> streams, boolean latest );

    List<String> listVersions( String name, InputStream inputStream, long modified );

    BundlerDependencies newBundlerDependencies( InputStream specs, long modified,
            InputStream prereleasedSpecs, long prereleasedModified );

    BundlerDependencies newBundlerDependencies();

}
