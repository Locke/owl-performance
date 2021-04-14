// SPDX-License-Identifier: MIT
package de.athalis.owl.performance.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OWLBenchmarkTestCase {
    private static final Logger logger = LoggerFactory.getLogger(OWLBenchmarkTestCase.class);

    private String name;
    private Integer warmups = -1;
    private Integer runs = -1;
    private List<OWLFile> files;

    @Override
    public String toString() {
        return "OWLBenchmarkTestCase{" +
                "name='" + name + '\'' +
                ", warmups=" + warmups +
                ", runs=" + runs +
                ", files=" + files +
                '}';
    }

    public void applyDefaults(int warmups, int runs) {
        if (warmups < 0) {
            throw new IllegalArgumentException("default warmups must not be negative");
        }
        if (runs < 0) {
            throw new IllegalArgumentException("default runs must not be negative");
        }

        if (this.warmups < 0) {
            this.warmups = warmups;
        }
        if (this.runs < 0) {
            this.runs = runs;
        }

        if (this.warmups == 0) {
            logger.info("case '" + name + "' has no warmups");
        }
        if (this.runs == 0) {
            logger.warn("case '" + name + "' has no runs");
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Integer getWarmups() {
        return warmups;
    }

    public void setWarmups(Integer warmups) {
        this.warmups = warmups;
    }

    public Integer getRuns() {
        return runs;
    }

    public void setRuns(Integer runs) {
        this.runs = runs;
    }

    public void setFiles(List<OWLFile> files) {
        this.files = files;
    }

    public List<OWLFile> getFiles() {
        return files;
    }
}
