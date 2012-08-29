package org.sonatype.nexus.plugins.ruby.fs;

import java.util.Collection;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.DefaultStorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;

class QuickMarshalStorageCollectionItem extends DefaultStorageCollectionItem
{

    private final Collection<StorageItem> list;

    public QuickMarshalStorageCollectionItem( Repository repository,
            ResourceStoreRequest request, Collection<StorageItem> list )
    {
        super( repository, request, true, false );
        this.list = list;
    }

    @Override
    public Collection<StorageItem> list()
    {
        return list;
    }
}