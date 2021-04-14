[![build status](https://travis-ci.com/Locke/owl-performance.svg?branch=main)](https://travis-ci.com/Locke/owl-performance)

# About

This is a prototype to measure reasoning times for different OWL ontology files and their combinations.

## History / background

Originally a quick & dirty measurement for reasoning times was needed to compare variants of an ontology [pass ont performance].
That original codebase had all ontology variants and benchmark test cases hardcoded.

To further investigate the performance variants of ontologies a more flexible definition of benchmark test cases was needed. 

Identified key elements for benchmarking the reasoning times of OWL ontology files:
* easy to define multiple ontologies and their variants together with benchmark test cases, e.g. to compare the reasoning time of the cartesian product between one parent ontology in three variants and multiple extending ontologies
* JVM-warmup / multiple runs (individually configurable, as reasoning time varies by several orders of magnitudes depending on the variants / considered extending ontologies)
* binding everything together (reading configuration, handling warmups/runs, initializing OWLAPI & Reasoner and measurement of the reasoning time)

Existing work was found to be focused on benchmarking reasoners itself, e.g. by using artificial ontologies.

Instead of looking too much into existing work I decided to generalize my original hard-coded codebase
and extend it with an easy to define configuration for benchmark test cases with YAML.


# Usage

Ontology files and benchmark test cases are defined in YAML.

Minimal configuration:

```yaml
files:
  - &file1
    iri: https://example.com/test.owl
    path: test.owl

cases:
  - case1
    name: simple benchmark test case
    files: [*file1]
```

See a full example: src/test/resources/example-01/

YAML settings:
* `defaultWarmups` (int; optional, default value `1`)
* `defaultRuns` (int; optional, default value `1`)
* `files` (list), each having:
  * `iri` (string): required for loading / merging ontologies
  * `name` (string; optional, currently unused)
  * `path` (string): path to owl file, relative to the yaml file
* `cases` (list), each having:
  * `name` (string)
  * `warmups` (int; optional, defaults to `defaultWarmups`): how often to perform reasoning without measuring the duration
  * `runs` (int; optional, defaults to `defaultRuns`): how often to perform reasoning with measuring the duration
  * `files` (list): references to the files defined earlier (technically, does not need to be references, but this reduces duplication)

The benchmark test cases will be executed and reported in the defined order.
As seen in example-01 setting `warmups` of the first benchmark and `runs` to 0 can be used to warmup the JVM with a comprehensive ontology, ignoring the result.

# License

See the file [LICENSE.md](LICENSE.md). Summary:

All code is released under the MIT license, which is provided in the file [LICENSE.MIT.txt](LICENSE.MIT.txt).

The example is based on the [Protégé Pizza Tutorial](https://www.michaeldebellis.com/post/new-protege-pizza-tutorial) and licensed under the GNU Affero General Public License v3.0, which is provided in the file [LICENSE.AGPL-3.0.md](LICENSE.AGPL-3.0.md).

# Development

Do not expect much development from my side, this was created for a single use-case.

## Building

* developed with OpenJDK 11, other JDKs / Java versions are untested but should work fine
* right now a SNAPSHOT version of Openllet is used, i.e. you might want to modify pom.xml for a stable release or build and install Openllet locally

Maven is used as build system; example usage:

```bash
mvn clean
mvn compile
mvn test
mvn package
```

## TODO / future 

Benchmark test case definition:
* feature: extend the config to define ontologies and their variants (instead of just `files`), which would allow to define the `iri` tag just on the ontology level
* feature: extend the cases to allow a matrix-definition, e.g. given two ontologies each with two variants to automatically define four cases from their cartesian product

Usage:
* output is printed with tab-separated columns at the end. This could be much nicer.

In general:
* the focus of this project should be the benchmark of ontologies, not benchmarking OWL reasoner
* investigate existing OWL benchmarking works more in depth. If possible, existing tools could be used as backend or be extended, rendering the following nice-to-have features obsolete
* code structure: currently everything is coupled very tightly 
* feature: support for different frameworks, e.g. Apache Jena as alternative / comparison to OWLAPI
* feature: support for different reasoners, e.g. as alternatives / comparisons to Openllet
