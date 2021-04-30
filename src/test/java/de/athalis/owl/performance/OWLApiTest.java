// SPDX-License-Identifier: MIT
package de.athalis.owl.performance;

import de.athalis.owl.performance.config.OWLBenchmarkTestCase;
import de.athalis.owl.performance.config.YamlConfigFile;
import de.athalis.owl.performance.config.YamlConfigFileTest;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class OWLApiTest {

    private static Logger logger = LoggerFactory.getLogger(OWLApiTest.class);

    private final static OwlAPIBenchmark owlAPIBenchmark;

    static {
        URL exampleConfigURL = YamlConfigFileTest.class.getClassLoader().getResource("example-01/config.yaml");
        assertNotNull(exampleConfigURL);

        YamlConfigFile exampleConfig = null;
        try {
            exampleConfig = YamlConfigFile.readYaml(exampleConfigURL);
        }
        catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        assertNotNull(exampleConfig);

        owlAPIBenchmark = new OwlAPIBenchmark(exampleConfig);
    }

    @BeforeAll
    public static void init() throws IOException, OWLOntologyCreationException {
        logger.info("init...");

        owlAPIBenchmark.init();

        logger.info("init done.");
    }

    @AfterAll
    public static void printResults() {
        System.out.println("Results:");
        owlAPIBenchmark.results.forEach(System.out::println);
    }


    private static Stream<Arguments> getTestData() {
        return owlAPIBenchmark.testData
                .entrySet()
                .stream()
                .map(e -> Arguments.of(e.getKey(), e.getValue()));
    }

    @ParameterizedTest
    @MethodSource("getTestData")
    public void runTests(OWLBenchmarkTestCase testCase, OWLOntology ont) throws Exception {
        logger.info("Test started: " + testCase.getName());

        owlAPIBenchmark.runTestCase(testCase, ont);
    }


}