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
     * Sets async reindexing for repository enabled or disabled. If disabled, the reindexRepository( repository ) will
     * not do anything, until reenabled. The call will update async indexer internal state, but will NOT perform actual
     * reindex.
     * 
     * @param enable true to enable, false to disable
     * @param repository
     */
    void setAsyncReindexingEnabled( boolean enable, RubyRepository repository );

    /**
     * Will perform the reindex in async mode, obeying the silentPeriod as described above.
     * 
     * @param repository
     * @param if true, update happens, oherwise full reindex
     */
    void reindexRepository( RubyRepository repository, boolean update );

    /**
     * Will perform the reindex in sync mode (immediately). This will block the caller thread, until Gem indexer
     * finishes.
     * 
     * @param repository
     * @param if true, update happens, oherwise full reindex
     */
    void reindexRepositorySync( RubyRepository repository, boolean update );
}
