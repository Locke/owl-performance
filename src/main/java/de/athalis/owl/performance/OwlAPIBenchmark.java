// SPDX-License-Identifier: MIT
package de.athalis.owl.performance;

import de.athalis.owl.performance.config.OWLBenchmarkTestCase;
import de.athalis.owl.performance.config.OWLFile;
import de.athalis.owl.performance.config.YamlConfigFile;

import java.io.IOException;
import java.util.*;

import openllet.core.OpenlletOptions;
import openllet.owlapi.OpenlletReasoner;
import openllet.owlapi.OpenlletReasonerFactory;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OwlAPIBenchmark {

    private final static Logger logger = LoggerFactory.getLogger(OwlAPIBenchmark.class);

    // preserve order
    public final HashMap<OWLBenchmarkTestCase, OWLOntology> testData = new LinkedHashMap<>();
    public final List<String> results = new LinkedList<>();

    private final YamlConfigFile config;

    private final ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
    private final OWLReasonerConfiguration reasonerConfig = new SimpleConfiguration(progressMonitor, FreshEntityPolicy.ALLOW,
            Long.MAX_VALUE, IndividualNodeSetPolicy.BY_SAME_AS);

    public OwlAPIBenchmark(YamlConfigFile config) {
        if (config.getCases() == null || config.getCases().isEmpty()) {
            throw new IllegalArgumentException("no benchmark test cases in configuration");
        }

        this.config = config;
    }

    public void init() throws IOException, OWLOntologyCreationException {
        logger.info("init...");

        // workaround for https://github.com/Galigator/openllet/issues/38
        // NOTE: that error did not occur during the performance tests, maybe some changes to the ontology "solved" this since I originally encountered it in a different application
        OpenlletOptions.TRACK_BRANCH_EFFECTS = true;

        // throw UnsupportedFeatureException for unsupported axioms
        OpenlletOptions.IGNORE_UNSUPPORTED_AXIOMS = false;

        for (OWLBenchmarkTestCase testCase : this.config.getCases()) {
            // NOTE: may throw Exception, for example if an ontology cannot be imported
            OWLOntologyManager manager = OwlAPIHelper.createPreloadedManager(testCase);

            OWLOntology ont = manager.createOntology();

            for (OWLFile f : testCase.getFiles()) {
                OWLImportsDeclaration importDeclaration = manager.getOWLDataFactory().getOWLImportsDeclaration(IRI.create(f.getIri()));
                manager.applyChange(new AddImport(ont, importDeclaration));
            }

            logger.info(testCase.getName() + ": created merged model");

            testData.put(testCase, ont);
        }

        if (testData.isEmpty()) {
            throw new InternalError("no benchmark test cases loaded");
        }

        logger.info("init done.");
    }

    public void runTestCase(OWLBenchmarkTestCase testCase, OWLOntology ont) throws Exception {
        String testCaseBaseName = testCase.getName();

        int w = 0;
        int warmups = testCase.getWarmups();
        long warmupDuration = 0;

        int r = 0;
        int runs = testCase.getRuns();
        long runDuration = 0;

        boolean failure = false;

        logger.info("[" + testCaseBaseName + "]: starting warmups...");

        while (!failure && (w < warmups)) {
            w++;
            String testCaseName = testCaseBaseName + ", warmup " + w + "/" + warmups;
            long t = measureReasoningDuration(ont, testCaseName);

            if (t < 0) {
                failure = true;
            }
            else {
                warmupDuration += t;
            }
        }

        logger.info("[" + testCaseBaseName + "]: warmup took " + niceTime(warmupDuration));
        logger.info("[" + testCaseBaseName + "]: starting runs...");

        while (!failure && (r < runs)) {
            r++;
            String testCaseName = testCaseBaseName + ", run " + r + "/" + runs;
            long t = measureReasoningDuration(ont, testCaseName);

            if (t < 0) {
                failure = true;
            }
            else {
                runDuration += t;
            }
        }

        logger.info("[" + testCaseBaseName + "]: runs took " + niceTime(runDuration));

        if (failure) {
            results.add(testCaseBaseName + "\tn.a.\tfailed");
        }
        else {
            if (runs > 0) {
                long t_ns = runDuration / runs;
                long t_ms = t_ns / (1000*1000);
                results.add(testCaseBaseName + "\t" + t_ms + "\tpassed\t" + w + "\t" + r);
            }
            else {
                results.add(testCaseBaseName + "\tn.a.\tpassed\t" + w + "\t" + r);
            }
        }
    }

    private long measureReasoningDuration(OWLOntology ont, String testCaseName) throws Exception {
        logger.info("[" + testCaseName + "]: starting test case...");

        OpenlletReasoner reasoner = OpenlletReasonerFactory.getInstance().createNonBufferingReasoner(ont, reasonerConfig);

        logger.info("[" + testCaseName + "]: created reasoner instance, checking consistency...");

        if (!reasoner.isConsistent()) {
            results.add(testCaseName + "\tinconsistent");
            throw new Exception(testCaseName + ": inconsistent");
        }
        else {
            logger.info("[" + testCaseName + "]: consistent, precomputeInferences...");

            long t1 = System.nanoTime();
            long precomputeInferencesDuration;
            Exception ex = null;
            try {
                reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
                precomputeInferencesDuration = System.nanoTime() - t1;
            }
            catch (Exception ex2) {
                precomputeInferencesDuration = System.nanoTime() - t1;
                ex = ex2;
                ex2.printStackTrace(System.err);
            }

            logger.info("[" + testCaseName + "]: precomputeInferences took " + niceTime(precomputeInferencesDuration));

            if (ex != null) {
                // TODO: just keep as result. Throwing makes debugging easier though
                throw new Exception(testCaseName + ": exception occurred", ex);
                // return -1;
            }
            else {
                return precomputeInferencesDuration;
            }
        }
    }

    private static String niceTime(long durationNanoSeconds) {
        long seconds = Math.round(durationNanoSeconds / 1e9);
        if (seconds > 10) {
            return String.format("%02d:%02d", seconds / 60, seconds % 60);
        }
        else {
            return Math.round(durationNanoSeconds / 1e6) + " ms";
        }
    }
}
