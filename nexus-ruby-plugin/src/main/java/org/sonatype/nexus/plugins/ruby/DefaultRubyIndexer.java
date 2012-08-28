package org.sonatype.nexus.plugins.ruby;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
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
    private RubyGateway rubyGateway;

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

    public void setAsyncReindexingEnabled( boolean enable, RubyRepository repository )
    {
        if ( enable )
        {
            getIndexerThread( repository ).enable();
        }
        else
        {
            getIndexerThread( repository ).disable();
        }
    }

    public void reindexRepository( RubyRepository repository, boolean update )
    {
        getIndexerThread( repository ).reindex( update );
    }

    public void reindexRepositorySync( RubyRepository repository, boolean update )
    {
        reindexRepositorySync( true, repository, update );
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

                indexers.remove( revt.getRepository().getId() );
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

    protected IndexerThread getIndexerThread( RubyRepository repository )
    {
        IndexerThread newGuy = new IndexerThread( repository );

        IndexerThread indexerThread = indexers.putIfAbsent( repository.getId(), newGuy );

        if ( indexerThread == null )
        {
            indexerThread = newGuy;

            getLogger().debug("Starting new indexer thread");

            indexerThread.start();
        }

        return indexerThread;
    }

    protected synchronized void reindexRepositorySync( boolean fromUser, RubyRepository repository, boolean update )
    {
        if ( !fromUser && !getIndexerThread( repository ).isSilentPeriodOver() )
        {
            return;
        }

        if ( fromUser )
        {
            getIndexerThread( repository ).disable();
        }

        try
        {
     //       rubyGateway.gemGenerateIndexes( ( (DefaultFSLocalRepositoryStorage) repository.getLocalStorage() )
       //             .getBaseDir( repository, new ResourceStoreRequest( "/" ) ), update );

            repository.getNotFoundCache().purge();
        }
//        catch ( LocalStorageException e )
//        {
//            DefaultRubyIndexer.this.getLogger().warn(
//                "Could not generate RubyGems index! Index may be stale, and change is not reflected!", e );
//        }
        finally
        {
            if ( fromUser )
            {
                getIndexerThread( repository ).enable();
            }
        }
    }

    public class IndexerThread
        extends Thread
    {
        private RubyRepository repository;

        private volatile long lastReindex = 0;

        private volatile boolean active = true;

        private volatile boolean done = true;

        private volatile boolean disabled = false;

        private volatile boolean update = true;

        public IndexerThread( RubyRepository repository )
        {
            this.repository = repository;
        }

        public void reindex( boolean update )
        {
            this.done = false;

            this.update = update;
        }

        public void reindexNow()
        {
            DefaultRubyIndexer.this.reindexRepositorySync( false, this.repository, this.update );

            this.done = true;
        }

        public void shutdown()
        {
            this.active = false;
        }

        public boolean isDisabled()
        {
            return disabled;
        }

        public void enable()
        {
            this.disabled = false;
        }

        public void disable()
        {
            this.disabled = true;
        }

        public boolean isSilentPeriodOver()
        {
            return !isDisabled()
                && System.currentTimeMillis() - lastReindex > DefaultRubyIndexer.this.getSilentPeriod();
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

                    this.active = false;
                }

                if ( active && !done && isSilentPeriodOver() )
                {
                    reindexNow();
                    this.lastReindex = System.currentTimeMillis();
                }
            }
        }
    }
}
