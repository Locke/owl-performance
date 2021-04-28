// SPDX-License-Identifier: MIT
package de.athalis.owl.performance;

import de.athalis.owl.performance.config.OWLBenchmarkTestCase;
import de.athalis.owl.performance.config.YamlConfigFile;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.Map;

import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.LoaderOptions;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        long initStartTime = System.nanoTime();

        if (args.length < 1 || args.length > 2) {
            throw new IllegalArgumentException("needs first parameter as path to yaml config file and optionally second parameter for yaml maxAliasesForCollections limit");
        }

        String pathRaw = args[0];
        String limitRaw = args.length > 1 ? args[1] : null;

        LoaderOptions loadingConfig = new LoaderOptions();
        if (limitRaw != null && !limitRaw.isBlank()) {
            try {
                int limit = Integer.parseInt(limitRaw, 10);
                loadingConfig.setMaxAliasesForCollections(limit);
            }
            catch (Exception ex) {
                logger.warn("unable to parse yaml limit", ex);
            }
        }

        logger.info("maxAliasesForCollections: " + loadingConfig.getMaxAliasesForCollections());

        try {
            URI uri;
            File file = new File(pathRaw);
            logger.debug("trying if local file: " + file);

            if (file.exists()) {
                logger.debug("file exists");
                uri = file.toURI();
            }
            else {
                logger.debug("file does not exist, assuming URI");
                uri = new URI(pathRaw);
            }

            URL url = uri.normalize().toURL();

            logger.info("loading config from: " + url);
            YamlConfigFile config = YamlConfigFile.readYaml(url, loadingConfig);
            logger.debug("config loaded");

            OwlAPIBenchmark owlAPIBenchmark = new OwlAPIBenchmark(config);

            owlAPIBenchmark.init();

            long initEndTime = System.nanoTime();
            logger.info("init took " + Util.niceTime(initEndTime - initStartTime));

            long startTime = System.nanoTime();

            for (Map.Entry<OWLBenchmarkTestCase, OWLOntology> e : owlAPIBenchmark.testData.entrySet()) {
                logger.info("test case started: " + e.getKey().getName());

                owlAPIBenchmark.runTestCase(e.getKey(), e.getValue());
            }

            long endTime = System.nanoTime();
            logger.info("all test cases took " + Util.niceTime(endTime - startTime));

            System.out.println("Results:");
            System.out.println("case\twarmupDuration_ms\trunDuration_ms\tavg_ms\tresult\twarmups\truns");
            owlAPIBenchmark.results.forEach(System.out::println);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
