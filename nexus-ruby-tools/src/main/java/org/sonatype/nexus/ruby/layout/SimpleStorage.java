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
package org.sonatype.nexus.ruby.layout;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.util.zip.GZIPInputStream;

import org.sonatype.nexus.ruby.DependencyFile;
import org.sonatype.nexus.ruby.Directory;
import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.SpecsIndexFile;
import org.sonatype.nexus.ruby.SpecsIndexZippedFile;

/**
 * simple storage implementation using the system's filesystem.
 * it uses <code>InputStream</code>s as payload.
 *
 * @author christian
 */
public class SimpleStorage
    implements Storage
{
  private final SecureRandom random = new SecureRandom();

  private final File basedir;

  /**
   * create the storage with given base-directory.
   */
  public SimpleStorage(File basedir) {
    this.basedir = basedir;
    this.random.setSeed(System.currentTimeMillis());
  }

  @Override
  public InputStream getInputStream(RubygemsFile file) throws IOException {
    if (file.hasException()) {
      throw new IOException(file.getException());
    }
    InputStream is;
    if (file.get() == null) {
      is = Files.newInputStream(toPath(file));
    }
    else {
      is = (InputStream) file.get();
    }
    // reset state since we have a payload and no exceptions
    file.resetState();
    return is;
  }

  /**
   * convert <code>RubygemsFile</code> into a <code>Path</code>.
   */
  protected Path toPath(RubygemsFile file) {
    return new File(basedir, file.storagePath()).toPath();
  }

  @Override
  public long getModified(RubygemsFile file) {
    return toPath(file).toFile().lastModified();
  }

  @Override
  public void retrieve(RubygemsFile file) {
    file.resetState();

    if (Files.notExists(toPath(file))) {
      file.markAsNotExists();
    }
    try {
      file.set(getInputStream(file));
    }
    catch (NoSuchFileException e) {
      file.markAsNotExists();
    }
    catch (IOException e) {
      file.setException(e);
    }
  }

  @Override
  public void retrieve(DependencyFile file) {
    retrieve((RubygemsFile) file);
  }

  @Override
  public void retrieve(SpecsIndexZippedFile file) {
    retrieve((RubygemsFile) file);
  }

  @Override
  public void retrieve(SpecsIndexFile file) {
    SpecsIndexZippedFile zipped = file.zippedSpecsIndexFile();
    retrieve(zipped);
    if (zipped.notExists()) {
      file.markAsNotExists();
    }
    if (zipped.hasException()) {
      file.setException(zipped.getException());
    }
    try {
      file.set(new GZIPInputStream(getInputStream(zipped)));
    }
    catch (IOException e) {
      file.setException(e);
    }
  }

  @Override
  public void create(InputStream is, RubygemsFile file) {
    Path target = toPath(file);
    Path mutex = target.resolveSibling(target.getFileName() + ".lock");
    Path source = target.resolveSibling("tmp." + Math.abs(random.nextLong()));
    try {
      createDirectory(source.getParent());
      Files.createFile(mutex);
      Files.copy(is, source);
      Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
      file.set(Files.newInputStream(target));
    }
    catch (FileAlreadyExistsException e) {
      mutex = null;
      file.markAsTempUnavailable();
    }
    catch (IOException e) {
      file.setException(e);
    }
    finally {
      if (mutex != null) {
        mutex.toFile().delete();
      }
      source.toFile().delete();
    }
  }

  @Override
  public void update(InputStream is, RubygemsFile file) {
    Path target = toPath(file);
    Path source = target.resolveSibling("tmp." + Math.abs(random.nextLong()));
    try {
      createDirectory(source.getParent());
      Files.copy(is, source);
      Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
      file.set(Files.newInputStream(target));
    }
    catch (IOException e) {
      file.setException(e);
    }
    finally {
      source.toFile().delete();
    }
  }

  /**
   * create a directory if it is not existing
   */
  protected void createDirectory(Path parent) throws IOException {
    if (!Files.exists(parent)) {
      Files.createDirectories(parent);
    }
  }

  @Override
  public void delete(RubygemsFile file) {
    try {
      Files.deleteIfExists(toPath(file));
    }
    catch (IOException e) {
      file.setException(e);
    }
  }

  @Override
  public void memory(InputStream data, RubygemsFile file) {
    file.set(data);
  }

  @Override
  public void memory(String data, RubygemsFile file) {
    memory(new ByteArrayInputStream(data.getBytes()), file);
  }

  @Override
  public String[] listDirectory(Directory dir) {
    return toPath(dir).toFile().list();
  }

}