// SPDX-License-Identifier: MIT
package de.athalis.owl.performance;

import de.athalis.owl.performance.config.OWLBenchmarkTestCase;
import de.athalis.owl.performance.config.OWLFile;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class OwlAPIHelper {
    private static final Logger logger = LoggerFactory.getLogger(OwlAPIHelper.class);

    public static OWLOntologyManager createPreloadedManager(OWLBenchmarkTestCase testCase) throws IOException, OWLOntologyCreationException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        for (OWLFile f : testCase.getFiles()) {
            logger.info("[" + testCase.getName() + "] loading: " + f);

            try (InputStream ontIn = f.getPathURI().toURL().openStream()) {
                IRI iri = IRI.create(f.getIri());
                StreamDocumentSource s = new StreamDocumentSource(ontIn, iri);
                OWLOntology ont = manager.loadOntologyFromOntologyDocument(s);

                logger.info("[" + testCase.getName() + "] loaded to manager: " + ont.getOntologyID().getOntologyIRI());
            }
        }

        return manager;
    }
}
