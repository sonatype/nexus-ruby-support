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

    /**
     * Will perform the reindex in async mode, obeying the silentPeriod as described above.
     * 
     * @param repository
     */
    void reindexRepository( RubyRepository repository );

    /**
     * Will perform the reindex in sync mode (immediately). This will block the caller thread, until Gem indexer
     * finishes.
     * 
     * @param repository
     */
    void reindexRepositorySync( RubyRepository repository );
}
