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

    private List<OWLFile> files;
    private List<OWLBenchmarkTestCase> cases;

    public static YamlConfigFile readYaml(final URL configFileURL) throws IOException, URISyntaxException {
        Yaml yaml = new Yaml(new Constructor(YamlConfigFile.class));

        try (InputStream inputStream = configFileURL.openStream()) {
            YamlConfigFile obj = yaml.load(inputStream);

            URI root = configFileURL.toURI().resolve(".");

            obj.setRoot(root);

            return obj;
        }
    }

    private void setRoot(URI root) {
        files.forEach(f -> f.setRoot(root));
    }

    @Override
    public String toString() {
        return "YamlConfigFile{" +
                "files=" + files +
                ", cases=" + cases +
                '}';
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
