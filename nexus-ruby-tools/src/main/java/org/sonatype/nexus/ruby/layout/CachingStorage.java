package org.sonatype.nexus.ruby.layout;

import java.io.File;
import java.net.URL;

/**
 * @author christian
 * @deprecated use CachingProxyStorage instead.
 */
@Deprecated
public class CachingStorage
    extends CachingProxyStorage
{
  public CachingStorage(File basedir, URL baseurl) {
    super(basedir, baseurl);
  }

  public CachingStorage(File basedir, URL baseurl, long ttl) {
    super(basedir, baseurl, ttl);
  }
}