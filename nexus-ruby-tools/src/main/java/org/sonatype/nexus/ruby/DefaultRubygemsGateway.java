/*
 * Copyright (c) 2007-2014 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.nexus.ruby;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jruby.embed.PathType;
import org.jruby.embed.ScriptingContainer;
import org.jruby.embed.osgi.OSGiScriptingContainer;
import org.jruby.runtime.builtin.IRubyObject;
import org.osgi.framework.FrameworkUtil;

public class DefaultRubygemsGateway
    extends ScriptWrapper
    implements RubygemsGateway
{
  private static ScriptingContainer newScriptingContainer() {
    ScriptingContainer container;
    try {
      container = new OSGiScriptingContainer(FrameworkUtil.getBundle(DefaultRubygemsGateway.class));
    }
    catch (Throwable e) {
      container = new ScriptingContainer();
    }
    // set the right classloader
    container.setClassLoader(DefaultRubygemsGateway.class.getClassLoader());

    return container;
  }

  public DefaultRubygemsGateway() {
    this(newScriptingContainer());
  }

  public DefaultRubygemsGateway(ScriptingContainer container) {
    super(container);
  }

  protected Object newScript() {
    IRubyObject nexusRubygemsClass = scriptingContainer.parse(PathType.CLASSPATH, "nexus/rubygems.rb").run();
    return scriptingContainer.callMethod(nexusRubygemsClass, "new", Object.class);
  }

  @Override
  public DependencyData dependencies(InputStream is, String name, long modified) {
    return new DependencyDataImpl(scriptingContainer,
        callMethod("dependencies", new Object[]{name, is}, Object.class),
        modified);
  }

  @Override
  public InputStream emptyIndex() {
    @SuppressWarnings("unchecked")
    List<Long> array = (List<Long>) callMethod("empty_specs", List.class);

    return new ByteArrayInputStream(array);
  }

  @Override
  public Object spec(InputStream gem) {
    try {
      return callMethod("spec_get", gem, Object.class);
    }
    finally {
      IOUtil.close(gem);
    }
  }

  @SuppressWarnings("resource")
  @Override
  public InputStream addSpec(Object spec, InputStream specsIndex, SpecsIndexType type) {
    try {
      @SuppressWarnings("unchecked")
      List<Long> array = (List<Long>) callMethod("add_spec",
          new Object[]{
              spec,
              specsIndex,
              type.name().toLowerCase()
          },
          List.class);

      return array == null ? null : new ByteArrayInputStream(array);
    }
    finally {
      IOUtil.close(specsIndex);
    }
  }

  @Override
  public InputStream deleteSpec(Object spec, InputStream specsIndex) {
    return deleteSpec(spec, specsIndex, null);
  }

  @SuppressWarnings("resource")
  @Override
  public InputStream deleteSpec(Object spec,
                                InputStream specsIndex,
                                InputStream releasesSpecs)
  {
    try {
      @SuppressWarnings("unchecked")
      List<Long> array = (List<Long>) callMethod("delete_spec",
          new Object[]{
              spec,
              specsIndex,
              releasesSpecs
          },
          List.class);

      return array == null ? null : new ByteArrayInputStream(array);
    }
    finally {
      IOUtil.close(specsIndex);
    }
  }

  @SuppressWarnings("resource")
  @Override
  public InputStream mergeSpecs(List<InputStream> streams,
                                boolean latest)
  {
    try {
      @SuppressWarnings("unchecked")
      List<Long> array = (List<Long>) callMethod("merge_specs",
          new Object[]{
              streams,
              latest
          },
          List.class);

      return array == null ? null : new ByteArrayInputStream(array);
    }
    finally {
      for (InputStream in : streams) {
        IOUtil.close(in);
      }
    }
  }

  @Override
  public Map<String, InputStream> splitDependencies(InputStream deps) {
    try {
      @SuppressWarnings("unchecked")
      Map<String, List<Long>> map = (Map<String, List<Long>>) callMethod("split_dependencies",
          deps,
          Map.class);

      Map<String, InputStream> result = new HashMap<>();
      for (Map.Entry<String, List<Long>> entry : map.entrySet()) {
        result.put(entry.getKey(), new ByteArrayInputStream(entry.getValue()));
      }
      return result;
    }
    finally {
      IOUtil.close(deps);
    }
  }

  @Override
  public InputStream mergeDependencies(List<InputStream> deps) {
    return mergeDependencies(deps, false);
  }


  @SuppressWarnings("resource")
  @Override
  public InputStream mergeDependencies(List<InputStream> deps, boolean unique) {
    try {
      Object[] args = new Object[deps.size() + 1];
      args[0] = unique;
      int index = 1;
      for (InputStream is : deps) {
        args[index++] = is;
      }
      @SuppressWarnings("unchecked")
      List<Long> array = (List<Long>) callMethod("merge_dependencies",
          args,
          List.class);

      return array == null ? null : new ByteArrayInputStream(array);
    }
    finally {
      for (InputStream in : deps) {
        IOUtil.close(in);
      }
    }
  }

  @SuppressWarnings("resource")
  @Override
  public InputStream createDependencies(List<InputStream> gemspecs) {
    try {
      @SuppressWarnings("unchecked")
      List<Long> array = (List<Long>) (gemspecs.size() == 0 ?
          callMethod("create_dependencies",
              List.class) :
          callMethod("create_dependencies",
              gemspecs.toArray(),
              List.class));

      return array == null ? null : new ByteArrayInputStream(array);
    }
    finally {
      for (InputStream in : gemspecs) {
        IOUtil.close(in);
      }
    }
  }

  @Override
  public String pom(InputStream specRz, boolean snapshot) {
    try {
      return callMethod("to_pom", new Object[]{specRz, snapshot}, String.class);
    }
    finally {
      IOUtil.close(specRz);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public synchronized List<String> listAllVersions(String name,
                                                   InputStream inputStream,
                                                   long modified,
                                                   boolean prerelease)
  {
    try {
      return (List<String>) callMethod("list_all_versions",
          new Object[]{
              name,
              inputStream,
              modified,
              prerelease
          },
          List.class);
    }
    finally {
      IOUtil.close(inputStream);
    }
  }

  @Override
  public void recreateRubygemsIndex(String directory) {
    callMethod("recreate_rubygems_index", directory, Void.class);
  }

  @Override
  public void purgeBrokenDepencencyFiles(String directory) {
    callMethod("purge_broken_depencency_files", directory, Void.class);
  }

  @Override
  public void purgeBrokenGemspecFiles(String directory) {
    callMethod("purge_broken_gemspec_files", directory, Void.class);
  }

  @Override
  public ByteArrayInputStream createGemspecRz(Object spec) {
    @SuppressWarnings("unchecked")
    List<Long> array = (List<Long>) callMethod("create_quick",
        new Object[]{spec},
        List.class);

    return new ByteArrayInputStream(array);
  }

  @Override
  public String filename(Object spec) {
    return scriptingContainer.callMethod(spec, "file_name", String.class);
  }

  @Override
  public String name(Object spec) {
    return scriptingContainer.callMethod(spec, "name", String.class);
  }
}
