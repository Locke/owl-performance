// SPDX-License-Identifier: MIT
package de.athalis.owl.performance.config;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class YamlConfigFileTest {

    @Test
    public void testExample01() throws IOException, URISyntaxException {
        URL exampleConfigURL = YamlConfigFileTest.class.getClassLoader().getResource("example-01/config.yaml");
        assertNotNull(exampleConfigURL);

        YamlConfigFile exampleConfig = YamlConfigFile.readYaml(exampleConfigURL);
        assertNotNull(exampleConfig);

        List<OWLBenchmarkTestCase> testCases = exampleConfig.getCases();
        assertNotNull(testCases);

        assertEquals(3, testCases.size());
        for (OWLBenchmarkTestCase testCase : testCases) {
            List<OWLFile> files = testCase.getFiles();
            assertNotNull(files);

            assertTrue(files.size() > 0 && files.size() < 4);

            for (OWLFile file : files) {
                assertNotNull(file.getIri());
                assertNotNull(file.getPathURI());
            }
        }
    }

}
