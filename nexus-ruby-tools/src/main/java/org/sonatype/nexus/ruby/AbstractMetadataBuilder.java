package org.sonatype.nexus.ruby;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class AbstractMetadataBuilder
{
  static SimpleDateFormat formater = new SimpleDateFormat("yyyyMMddHHmmss");

  static {
    formater.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  protected final String timestamp;

  public AbstractMetadataBuilder(long modified) {
    super();
    timestamp = formater.format(new Date(modified));
  }
}
