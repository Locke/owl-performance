// SPDX-License-Identifier: MIT
package de.athalis.owl.performance.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class YamlConfigFile {

    private boolean defaultDebug = false;
    private Integer defaultWarmups = 1;
    private Integer defaultRuns = 1;

    private List<OWLFile> files;
    private List<OWLBenchmarkTestCase> cases;

    public static YamlConfigFile readYaml(final URL configFileURL) throws IOException, URISyntaxException {
        Yaml yaml = new Yaml(new Constructor(YamlConfigFile.class));

        try (InputStream inputStream = configFileURL.openStream()) {
            YamlConfigFile obj = yaml.load(inputStream);

            URI root = configFileURL.toURI().resolve(".");

            obj.setRoot(root);
            obj.applyDefaults();

            return obj;
        }
    }

    private void setRoot(URI root) {
        files.forEach(f -> f.setRoot(root));
    }

    private void applyDefaults() {
        if (this.defaultWarmups < 0) {
            throw new IllegalArgumentException("default warmups must not be negative");
        }
        if (this.defaultRuns < 0) {
            throw new IllegalArgumentException("default runs must not be negative");
        }

        this.cases.forEach(f -> f.applyDefaults(this.defaultDebug, this.defaultWarmups, this.defaultRuns));
    }

    @Override
    public String toString() {
        return "YamlConfigFile{" +
                "defaultDebug=" + defaultDebug +
                ", defaultWarmups=" + defaultWarmups +
                ", defaultRuns=" + defaultRuns +
                ", files=" + files +
                ", cases=" + cases +
                '}';
    }

    public boolean isDefaultDebug() {
        return defaultDebug;
    }

    public void setDefaultDebug(boolean defaultDebug) {
        this.defaultDebug = defaultDebug;
    }

    public Integer getDefaultWarmups() {
        return defaultWarmups;
    }

    public void setDefaultWarmups(Integer defaultWarmups) {
        this.defaultWarmups = defaultWarmups;
    }

    public Integer getDefaultRuns() {
        return defaultRuns;
    }

    public void setDefaultRuns(Integer defaultRuns) {
        this.defaultRuns = defaultRuns;
    }

    public void setFiles(List<OWLFile> files) {
        this.files = files;
    }

    public List<OWLFile> getFiles() {
        return files;
    }

    public void setCases(List<OWLBenchmarkTestCase> cases) {
        this.cases = cases;
    }

    public List<OWLBenchmarkTestCase> getCases() {
        return cases;
    }
}
