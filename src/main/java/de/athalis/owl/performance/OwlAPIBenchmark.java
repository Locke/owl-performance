// SPDX-License-Identifier: MIT
package de.athalis.owl.performance;

import de.athalis.owl.performance.config.OWLBenchmarkTestCase;
import de.athalis.owl.performance.config.OWLFile;
import de.athalis.owl.performance.config.YamlConfigFile;

import java.io.File;
import java.util.*;

import openllet.core.OpenlletOptions;
import openllet.owlapi.OpenlletReasoner;
import openllet.owlapi.OpenlletReasonerFactory;
import openllet.owlapi.explanation.PelletExplanation;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.util.OWLOntologyMerger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OwlAPIBenchmark {

    private final static Logger logger = LoggerFactory.getLogger(OwlAPIBenchmark.class);

    // preserve order
    public final HashMap<OWLBenchmarkTestCase, OWLOntology> testData = new LinkedHashMap<>();
    public final List<String> results = new LinkedList<>();

    private final YamlConfigFile config;

    private final ReasonerProgressMonitor progressMonitorNormal = new NullReasonerProgressMonitor();
    private final ReasonerProgressMonitor progressMonitorDebug = new ConsoleProgressMonitor();

    private final OWLReasonerConfiguration reasonerConfigNormal = new SimpleConfiguration(progressMonitorNormal, FreshEntityPolicy.ALLOW, Long.MAX_VALUE, IndividualNodeSetPolicy.BY_SAME_AS);
    private final OWLReasonerConfiguration reasonerConfigDebug = new SimpleConfiguration(progressMonitorDebug, FreshEntityPolicy.ALLOW, Long.MAX_VALUE, IndividualNodeSetPolicy.BY_SAME_AS);

    public OwlAPIBenchmark(YamlConfigFile config) {
        if (config.getCases() == null || config.getCases().isEmpty()) {
            throw new IllegalArgumentException("no benchmark test cases in configuration");
        }

        this.config = config;
    }

    public void init() {
        logger.info("init...");

        // workaround for https://github.com/Galigator/openllet/issues/38
        // NOTE: that error did not occur during the performance tests, maybe some changes to the ontology "solved" this since I originally encountered it in a different application
        OpenlletOptions.TRACK_BRANCH_EFFECTS = true;

        // throw UnsupportedFeatureException for unsupported axioms
        OpenlletOptions.IGNORE_UNSUPPORTED_AXIOMS = false;

        for (OWLBenchmarkTestCase testCase : this.config.getCases()) {
            OWLOntology ont = null;

            try {
                OWLOntologyManager manager = OwlAPIHelper.createPreloadedManager(testCase);

                ont = manager.createOntology();

                for (OWLFile f : testCase.getFiles()) {
                    OWLImportsDeclaration importDeclaration = manager.getOWLDataFactory().getOWLImportsDeclaration(IRI.create(f.getIri()));
                    manager.applyChange(new AddImport(ont, importDeclaration));
                }

                logger.info("[" + testCase.getName() + "]: created merged model");
            }
            catch (Exception ex) {
                if (testCase.isDebug()) {
                    ex.printStackTrace(System.err);
                    throw new RuntimeException("failed to create merged model", ex);
                }
                else {
                    logger.warn("[" + testCase.getName() + "]: failed to create merged model", ex);
                }
            }

            testData.put(testCase, ont);
        }

        if (testData.isEmpty()) {
            throw new InternalError("no benchmark test cases loaded");
        }

        logger.info("init done.");
    }

    public void runTestCase(OWLBenchmarkTestCase testCase, OWLOntology ont) {
        String testCaseBaseName = testCase.getName();

        if (ont == null) {
            results.add(testCaseBaseName + "\tn.a.\tn.a.\tn.a.\tfailed\tn.a.\tn.a.");
        }
        else {
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
                long t = measureReasoningDuration(ont, testCaseName, testCase.isDebug());

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
                long t = measureReasoningDuration(ont, testCaseName, testCase.isDebug());

                if (t < 0) {
                    failure = true;
                }
                else {
                    runDuration += t;
                }
            }

            logger.info("[" + testCaseBaseName + "]: runs took " + niceTime(runDuration));

            long warmupDuration_ms = warmupDuration / (1000 * 1000);
            long runDuration_ms = runDuration / (1000 * 1000);

            if (failure) {
                results.add(testCaseBaseName + "\t" + warmupDuration_ms + "\t" + runDuration_ms + "\tn.a.\tfailed\t" + w + "\t" + r);
            }
            else {
                if (runs > 0) {
                    long avg_ns = runDuration / runs;
                    long avg_ms = avg_ns / (1000 * 1000);
                    results.add(testCaseBaseName + "\t" + warmupDuration_ms + "\t" + runDuration_ms + "\t" + avg_ms + "\tpassed\t" + w + "\t" + r);
                }
                else {
                    results.add(testCaseBaseName + "\t" + warmupDuration_ms + "\t" + runDuration_ms + "\tn.a.\tignored\t" + w + "\t" + r);
                }
            }
        }
    }

    private long measureReasoningDuration(OWLOntology ont, String testCaseName, boolean debug) {
        logger.info("[" + testCaseName + "]: starting test case...");

        OWLReasonerConfiguration reasonerConfig = debug ? reasonerConfigDebug : reasonerConfigNormal;
        OpenlletReasoner reasoner = OpenlletReasonerFactory.getInstance().createNonBufferingReasoner(ont, reasonerConfig);

        logger.info("[" + testCaseName + "]: created reasoner instance, checking consistency...");

        if (debug) {
            reasoner.getKB().setDoExplanation(true);
        }

        if (!reasoner.isConsistent()) {
            if (debug) {
                logger.error("[" + testCaseName + "]: inconsistent");

                try {
                    logger.info("[" + testCaseName + "]: dumping merged file...");
                    File tmp = File.createTempFile("merged", ".owl");
                    OWLOntologyManager manager = ont.getOWLOntologyManager();
                    OWLOntology ontMerged = new OWLOntologyMerger(manager).createMergedOntology(manager, null);
                    ontMerged.saveOntology(new RDFXMLDocumentFormat(), IRI.create(tmp));
                    logger.info("[" + testCaseName + "]: dumped to: " + tmp);
                }
                catch (Exception ex) {
                    // ignore, as that is just nice to have
                }

                int i = 0;
                try {
                    PelletExplanation expGen = new PelletExplanation(reasoner);
                    Set<Set<OWLAxiom>> ex = expGen.getInconsistencyExplanations(2);
                    for (Set<OWLAxiom> s : ex) {
                        i++;
                        logger.error("[" + testCaseName + "]: explanation #" + i + ": " + s);
                    }
                }
                catch (Exception ex) {
                    // ignore, as that is just nice to have
                }
                if (i == 0) {
                    logger.error("[" + testCaseName + "]: no explanations found");
                }
                throw new RuntimeException(testCaseName + ": inconsistent");
            }
            return -1;
        }
        else {
            if (debug) {
                reasoner.getKB().setDoExplanation(false);
            }

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
                if (debug) {
                    ex2.printStackTrace(System.err);
                }
            }

            logger.info("[" + testCaseName + "]: precomputeInferences took " + niceTime(precomputeInferencesDuration));

            if (ex != null) {
                if (debug) {
                    throw new RuntimeException(testCaseName + ": exception occurred", ex);
                }
                return -2;
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
