package org.sonatype.nexus.plugins.ruby;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventRemove;
import org.sonatype.nexus.proxy.storage.local.fs.DefaultFSLocalRepositoryStorage;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;
import org.sonatype.plexus.appevents.Event;
import org.sonatype.plexus.appevents.EventListener;

@Component( role = RubyIndexer.class )
public class DefaultRubyIndexer
    implements RubyIndexer, EventListener, Startable
{
    /**
     * Default silent period of 5 minutes.
     */
    private static final long SILENT_PERIOD = 1000 * 60 * 5;

    @Requirement
    private Logger logger;

    @Requirement
    private ApplicationEventMulticaster applicationEventMulticaster;

    @Requirement
    private RubyGateway rg;

    /**
     * This is the amount of millis to wait before reindexing. If no content change occured during this period, reindex
     * will happen, otherwise it will be again delayed for this silentPeriod.
     */
    private volatile long silentPeriod = SILENT_PERIOD;

    private ConcurrentMap<String, IndexerThread> indexers = new ConcurrentHashMap<String, IndexerThread>();

    protected Logger getLogger()
    {
        return logger;
    }

    public long getSilentPeriod()
    {
        return silentPeriod;
    }

    public void setSilentPeriod( long periodMillis )
    {
        this.silentPeriod = periodMillis;
    }

    public void reindexRepository( RubyRepository repository )
    {
        IndexerThread newGuy = new IndexerThread( repository, rg );

        IndexerThread indexerThread = indexers.putIfAbsent( repository.getId(), newGuy );

        if ( indexerThread == null )
        {
            indexerThread = newGuy;

            indexerThread.start();
        }

        indexerThread.reindex();
    }

    public void onEvent( Event<?> evt )
    {
        if ( evt instanceof RepositoryRegistryEventRemove )
        {
            // remove the thread too to not prevent GC doing it's job
            RepositoryRegistryEventRemove revt = (RepositoryRegistryEventRemove) evt;

            IndexerThread it = indexers.get( revt.getRepository().getId() );

            if ( it != null )
            {
                it.shutdown();
            }
        }
    }

    public void start()
        throws StartingException
    {
        applicationEventMulticaster.addEventListener( this );
    }

    public void stop()
        throws StoppingException
    {
        applicationEventMulticaster.removeEventListener( this );
    }

    // ==

    public class IndexerThread
        extends Thread
    {
        private RubyRepository repository;

        private RubyGateway rubyGateway;

        private volatile long lastReindexRequested;

        private volatile boolean active = true;

        private volatile boolean done = true;

        public IndexerThread( RubyRepository repository, RubyGateway rubyGateway )
        {
            this.repository = repository;

            this.rubyGateway = rubyGateway;
        }

        public void reindex()
        {
            this.lastReindexRequested = System.currentTimeMillis();

            this.done = false;
        }

        public void shutdown()
        {
            this.active = false;
        }

        @Override
        public void run()
        {
            while ( active && !isInterrupted() )
            {
                try
                {
                    // let's peek in every 5 seconds
                    Thread.sleep( 5000 );
                }
                catch ( InterruptedException e )
                {
                    DefaultRubyIndexer.this.getLogger().warn( "Ruby IndexerThread interrupted, bailing out.", e );

                    shutdown();
                }

                if ( !done )
                {
                    if ( System.currentTimeMillis() - lastReindexRequested > DefaultRubyIndexer.this.getSilentPeriod() )
                    {
                        reindex( repository );

                        done = true;
                    }
                }
            }

            // cleanup
            DefaultRubyIndexer.this.indexers.remove( repository.getId() );
        }

        protected void reindex( RubyRepository repository )
        {
            try
            {
                // shadow repo needs "lazy" indexing, others "real" indexing
                if ( repository.getRepositoryKind().isFacetAvailable( RubyShadowRepository.class ) )
                {
                    rubyGateway.gemGenerateLazyIndexes( ( (DefaultFSLocalRepositoryStorage) repository
                        .getLocalStorage() ).getBaseDir( repository, new ResourceStoreRequest( "/" ) ) );
                }
                else
                {
                    rubyGateway.gemGenerateIndexes( ( (DefaultFSLocalRepositoryStorage) repository.getLocalStorage() )
                        .getBaseDir( repository, new ResourceStoreRequest( "/" ) ) );
                }

                repository.getNotFoundCache().purge();
            }
            catch ( StorageException e )
            {
                DefaultRubyIndexer.this.getLogger().warn(
                    "Could not generate RubyGems index! Index may be stale, and change is not reflected!", e );
            }
        }
    }
}
