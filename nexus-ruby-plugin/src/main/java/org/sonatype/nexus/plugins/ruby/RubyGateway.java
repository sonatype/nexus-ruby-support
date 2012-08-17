package org.sonatype.nexus.plugins.ruby;

import java.io.File;

/**
 * This component is the central "ruby interaction" point, and is meant to focus all "ruby related" calls to make it
 * easy to swap out and use proper stuff instead. What we have now is POC nexus-ruby-tools, and gemGenerateIndexes is
 * not implemented. The "proper" stuff should use JRuby and invoke the proper Gem:: classes doing the actual work.
 *
 * @author cstamas
 */
public interface RubyGateway
{

    /**
     * Invokes "gem generate_index --directory=${basedir}". Should do essentially the same as the CLI command written above but descend into subdirectories as well.
     *
     * @param basedir
     * @param if true, update happens, otherwise full reindex
     */
    void gemGenerateIndexes( File basedir, boolean update );
}
