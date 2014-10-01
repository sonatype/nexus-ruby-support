package org.sonatype.nexus.ruby;

public class NotFoundFile
    extends RubygemsFile
{
  public NotFoundFile(RubygemsFileFactory factory, String path) {
    super(factory, FileType.NOT_FOUND, null, path, null);
    markAsNotExists();
  }
}