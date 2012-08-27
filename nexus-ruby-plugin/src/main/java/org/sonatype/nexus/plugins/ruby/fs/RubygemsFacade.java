package org.sonatype.nexus.plugins.ruby.fs;

import java.io.File;

import org.sonatype.nexus.plugins.ruby.RubyRepository;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;

public interface RubygemsFacade {

    void addGem(RubyRepository repository, RubyLocalRepositoryStorage storage,
            File gem) throws UnsupportedStorageOperationException,
            LocalStorageException;

    void removeGem(RubyRepository repository,
            RubyLocalRepositoryStorage storage, File gem)
            throws UnsupportedStorageOperationException, LocalStorageException;

    StorageFileItem retrieveSpecsIndex(RubyRepository repository,
            RubyLocalRepositoryStorage storage, SpecsIndexType type,
            boolean gzipped) throws ItemNotFoundException,
            LocalStorageException;

}