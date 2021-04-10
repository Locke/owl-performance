// SPDX-License-Identifier: MIT
package de.athalis.owl.performance.config;

import java.util.List;

public class OWLBenchmarkTestCase {

    private String name;
    private List<OWLFile> files;

    @Override
    public String toString() {
        return "OWLBenchmarkTestCase{" +
                "name='" + name + '\'' +
                ", files=" + files +
                '}';
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setFiles(List<OWLFile> files) {
        this.files = files;
    }

    public List<OWLFile> getFiles() {
        return files;
    }
}
