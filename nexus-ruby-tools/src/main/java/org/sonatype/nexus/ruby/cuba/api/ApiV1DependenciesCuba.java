package org.sonatype.nexus.ruby.cuba.api;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.cuba.Cuba;
import org.sonatype.nexus.ruby.cuba.State;

/**
 * cuba for /api/v1/dependencies
 *
 * @author christian
 */
public class ApiV1DependenciesCuba
    implements Cuba
{
  public static final String JSON_RZ = ".json.rz";

  private static Pattern FILE = Pattern.compile("^([^/]+)" + JSON_RZ + "$");

  /**
   * no sub-directories
   *
   * if there is query string with "gems" parameter then <code>BundlerApiFile</code> or
   * <code>DependencyFile</code> gets created.
   *
   * otherwise all {name}.json.rz will be created as <code>DependencyFile</code>
   *
   * the directory itself does not produce the directory listing - only the empty <code>Directory</code>
   * object.
   */
  @Override
  public RubygemsFile on(State state) {
    if (state.name.isEmpty()) {
      if (state.context.query.startsWith("gems=")) {
        if (state.context.query.contains(",")) {
          return state.context.factory.bundlerApiFile(state.context.query.substring(5));
        }
        else {
          return state.context.factory.dependencyFile(state.context.query.substring(5));
        }
      }
      return state.context.factory.directory(state.context.original);
    }
    Matcher m;
    if (state.name.length() == 1) {
      if (state.path.length() < 2) {
        return state.context.factory.directory(state.context.original);
      }
      m = FILE.matcher(state.path.substring(1));
    }
    else {
      m = FILE.matcher(state.name);
    }
    if (m.matches()) {
      return state.context.factory.dependencyFile(m.group(1));
    }
    return state.context.factory.notFound(state.context.original);
  }
}