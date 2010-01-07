package org.sonatype.nexus.plugins.ruby;

/**
 * A thin layer on top of actual RubyIndexer, that adds some "CI-like" behaviour with deferring indexing. The method
 * call reindexRepository() may not actually reindex, it will wait for silent period and then spawn reindex.
 * 
 * @author cstamas
 */
public interface RubyIndexer
{
    long getSilentPeriod();

    void setSilentPeriod( long periodMillis );

    void reindexRepository( RubyRepository repository );
}
