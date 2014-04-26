package org.sonatype.nexus.ruby;

import java.io.InputStream;
import java.util.List;

public interface RubygemsGateway {

    void recreateRubygemsIndex( String directory );

    void purgeBrokenDepencencyFiles( String directory );

    void purgeBrokenGemspecFiles( String directory );
    
    ByteArrayInputStream createGemspecRz( Object spec );
    
    InputStream emptyIndex();

    Object spec( InputStream gem );
    Object spec( InputStream gem, String gemname );
    
    String pom( InputStream specRz );

    InputStream addSpec( Object spec, InputStream specsDump, SpecsIndexType type );

    InputStream deleteSpec( Object spec, InputStream specsDump );

    InputStream deleteSpec( Object spec, InputStream specsIndex, InputStream refSpecs );

    InputStream mergeSpecs( List<InputStream> streams, boolean latest );

    InputStream mergeDependencies( List<InputStream> deps );

    InputStream mergeDependencies( List<InputStream> deps, boolean unique );

    InputStream createDependencies( List<InputStream> gemspecs );

    List<String> listVersions( String name, InputStream inputStream, long modified, boolean prerelease );

    String filename( Object spec );

    String name( Object spec );
    
    String gemnameWithPlatform( String gemname, String version, InputStream specs, long modified );

    DependencyData dependencies( InputStream inputStream, long modified );

    List<String> listAllVersions( String name, InputStream inputStream,
                                  long modified, boolean prerelease );


}
