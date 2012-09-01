package org.sonatype.nexus.plugins.ruby.nexus123;

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.plugins.ruby.fs.SpecsIndexType;
import org.testng.annotations.Test;

public class Nexus123DownloadITSkipped extends AbstractNexusIntegrationTest{

    protected File downloadSpecsIndex(String repoId, SpecsIndexType type, boolean gzipped, String targetDirectory ) throws IOException{
        String baseUrl = AbstractNexusIntegrationTest.nexusBaseUrl + REPOSITORY_RELATIVE_URL + repoId
        + "/";
        String filename = type.filename() + (gzipped ? ".gz" : "");
        return downloadFile(new URL(baseUrl + filename), 
                new File(targetDirectory, filename ).getAbsolutePath());
    }
    
    @Test
    public void testdownloadSpecs() throws Exception
    {
       File specs = downloadSpecsIndex( "test", SpecsIndexType.RELEASE, false, "target/downloads/test" );
       assertThat( "exists", specs.exists() );
    }

}
