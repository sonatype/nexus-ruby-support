package org.sonatype.nexus.ruby;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface RubygemsGateway
{
  void recreateRubygemsIndex(String directory);

  void purgeBrokenDepencencyFiles(String directory);

  void purgeBrokenGemspecFiles(String directory);

  ByteArrayInputStream createGemspecRz(Object spec);

  InputStream emptyIndex();

  Object spec(InputStream gem);

  String pom(InputStream specRz, boolean snapshot);

  InputStream addSpec(Object spec, InputStream specsDump, SpecsIndexType type);

  InputStream deleteSpec(Object spec, InputStream specsDump);

  InputStream deleteSpec(Object spec, InputStream specsIndex, InputStream refSpecs);

  InputStream mergeSpecs(List<InputStream> streams, boolean latest);

  Map<String, InputStream> splitDependencies(InputStream bundlerResult);

  InputStream mergeDependencies(List<InputStream> deps);

  InputStream mergeDependencies(List<InputStream> deps, boolean unique);

  InputStream createDependencies(List<InputStream> gemspecs);

  String filename(Object spec);

  String name(Object spec);

  DependencyData dependencies(InputStream inputStream, String name, long modified);

  List<String> listAllVersions(String name, InputStream inputStream, long modified, boolean prerelease);
}
