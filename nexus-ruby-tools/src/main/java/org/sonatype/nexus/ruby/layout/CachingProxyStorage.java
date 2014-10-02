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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.sonatype.nexus.ruby.BundlerApiFile;
import org.sonatype.nexus.ruby.DependencyFile;
import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.SpecsIndexZippedFile;

/**
 * it uses the <code>SimpleStorage</code> to cache the remote files.
 *
 * @author christian
 */
public class CachingProxyStorage
    extends SimpleStorage
    implements ProxyStorage
{
  private final ConcurrentMap<String, Lock> locks = new ConcurrentSkipListMap<String, Lock>();

  protected final String baseurl;

  private final long ttl;

  private final int timeout;

  public CachingProxyStorage(File basedir, URL baseurl) {
    this(basedir, baseurl, 3600000);
  }

  public CachingProxyStorage(File basedir, URL baseurl, long timeToLiveOfVolatileFiles) {
    super(basedir);
    this.baseurl = baseurl.toExternalForm().replaceFirst("/$", "");
    this.ttl = timeToLiveOfVolatileFiles;
    this.timeout = 60000;
  }

  @Override
  public boolean isExpired(DependencyFile file) {
    Path path = toPath(file);
    if (Files.notExists(path)) {
      return true;
    }
    try {
      long mod = Files.getLastModifiedTime(path).toMillis();
      long now = System.currentTimeMillis();
      return now - mod > this.ttl;
    }
    catch (IOException e) {
      return true;
    }
  }

  @Override
  public void retrieve(BundlerApiFile file) {
    try {
      file.set(toUrl(file).openStream());
    }
    catch (IOException e) {
      file.setException(e);
    }
  }

  @Override
  public void retrieve(DependencyFile file) {
    retrieveVolatile(file);
  }

  @Override
  public void retrieve(SpecsIndexZippedFile file) {
    retrieveVolatile(file);
  }

  @Override
  public void retrieve(RubygemsFile file) {
    super.retrieve(file);

    if (file.notExists()) {
      download(file);
    }
  }

  private void download(RubygemsFile file) {
    try {
      URLConnection url = toUrl(file).openConnection();
      create(url.getInputStream(), file);
      if (file.hasPayload()) {
        setLastModified(toPath(file), url);
        file.resetState();
        super.retrieve(file);
      }
    }
    catch (FileNotFoundException e) {
      file.markAsNotExists();
    }
    catch (IOException e) {
      file.setException(e);
    }
  }

  protected boolean retrieveVolatile(RubygemsFile file) {
    Path path = toPath(file);
    if (Files.notExists(path)) {
      download(file);
      return file.hasPayload();
    }
    try {
      long mod = Files.getLastModifiedTime(path).toMillis();
      long now = System.currentTimeMillis();
      if (now - mod > this.ttl) {
        update(file, path);
      }
      else {
        retrieve((RubygemsFile) file);
      }
    }
    catch (IOException e) {
      file.setException(e);
    }
    return file.hasPayload();
  }

  private Lock lock(RubygemsFile file) {
    Lock l = new ReentrantLock();
    Lock ll = locks.putIfAbsent(file.remotePath(), l);
    return ll == null ? l : ll;
  }

  private void unlock(RubygemsFile file) {
    Lock l = locks.remove(file.remotePath());
    if (l != null) {
      l.unlock();
    }
  }

  protected void update(RubygemsFile file, Path path) {
    Lock lock = lock(file);
    if (lock.tryLock()) {
      try {
        URLConnection url = toUrl(file).openConnection();
        update(url.getInputStream(), file);
        setLastModified(path, url);
      }
      catch (IOException e) {
        file.setException(e);
      }
      finally {
        unlock(file);
      }
    }
    else {
      try {
        if (!lock.tryLock(timeout, TimeUnit.MILLISECONDS)) {
          file.markAsTempUnavailable();
        }
        else {
          // assume nothing to be done
          lock.unlock();
        }
      }
      catch (InterruptedException e) {
        // ignore
      }
    }
  }

  protected void setLastModified(Path path, URLConnection url)
      throws IOException
  {
    long mod = url.getLastModified();
    if (mod == 0) {
      mod = System.currentTimeMillis();
    }
    Files.setLastModifiedTime(path, FileTime.fromMillis(mod));
  }

  protected URL toUrl(RubygemsFile file) throws MalformedURLException {
    return new URL(baseurl + file.remotePath());
  }
}