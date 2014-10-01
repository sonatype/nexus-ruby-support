package org.sonatype.nexus.ruby;

import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class MetadataSnapshotBuilderTest
    extends TestCase
{
  private MetadataSnapshotBuilder builder;

  @Before
  public void setUp() throws Exception {
    builder = new MetadataSnapshotBuilder("jbundler", "9.2.1", 1397660433050l);
  }

  @Test
  public void testXml() throws Exception {
    String xml = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("metadata-snapshot.xml"));
    //        System.err.println( builder.toString() );
    //        System.err.println( xml );
    assertThat(builder.toString(), equalTo(xml));
  }
}
