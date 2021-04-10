// SPDX-License-Identifier: MIT
package de.athalis.owl.performance.config;

import java.net.URI;

public class OWLFile {

    private URI root;

    private String name;
    private String iri;
    private String path;

    private URI pathURI;

    @Override
    public String toString() {
        return "OWLFile{" +
                "name='" + name + '\'' +
                ", iri='" + iri + '\'' +
                ", path='" + path + '\'' +
                ", root='" + root + '\'' +
                ", pathURI='" + pathURI + '\'' +
                '}';
    }

    protected void setRoot(URI root) {
        this.root = root;

        if (this.path != null) {
            this.setPath(this.path);
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setIri(String iri) {
        this.iri = iri;
    }

    public String getIri() {
        return iri;
    }

    public void setPath(String path) {
        this.path = path;

        if (this.root != null) {
            this.pathURI = this.root.resolve(path);
        }
    }

    public String getPath() {
        return path;
    }

    public URI getPathURI() {
        return pathURI;
    }
}
