[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.8192579.svg)](https://doi.org/10.5281/zenodo.8192579)


SSSOM Java - SSSOM library for Java
===================================

SSSOM-Java is an implementation of the [Simple Standard for Sharing
Ontology Mappings (SSSOM)](https://mapping-commons.github.io/sssom/)
specification for the Java language – just a hobby, won’t be big and
professional like `sssom-py`.

The project provides a Java library that can be used to support the
SSSOM standard in a Java application, a program to manipulate mapping
sets from the command line, and a plugin for the
[ROBOT](http://robot.obolibrary.org/) ontology manipulation tool.

Java library
------------
The library allows to read and write mapping sets from/to any of the
following supported serialisation formats:

* SSOM/TSV;
* SSSOM/JSON;
* RDF/Turtle.

For example, reading a mapping set from a SSSOM/TSV file:

```java
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.TSVReader;
import org.incenp.obofoundry.sssom.SSSOMFormatException;

[...]
MappingSet mappingSet;
try {
    TSVReader reader = new TSVReader("my-mappings.sssom.tsv");
    mappingSet = reader.read();
} catch (SSSOMFormatException e) {
    // Invalid SSSOM data
    return;
}

// play with the mapping set
for (Mapping m : mappingSet.getMappings()) {
    System.out.printf("%s -[%s]-> %s\n", m.getSubjectId(), \
                                         m.getPredicateId(), \
                                         m.getObjectId());
}
```

The library has **full support** for the entire SSSOM specification,
including [extensions](https://mapping-commons.github.io/sssom/dev/spec-model/#non-standard-slots).

It also provides a domain-specific language for the manipulation of
SSSOM mappings: [SSSOM/Transform]((https://incenp.org/dvlpt/sssom-java/sssom-transform.html)
(SSSOM/T).

Command-line tool
-----------------
A Unix-like filter command that takes one or more mapping set(s) as
input and produces another mapping set as output, and can be used to,
among other things:

* validate mapping sets;

* merge several mapping sets into one;

* convert a set from one format to another.

The command additionally allows to perform arbitrary manipulations on
mappings through the SSSOM/Transform language.

ROBOT plugin
------------
The ROBOT plugin adds three commands to the ROBOT command set:

* A command to extract SSSOM mappings from a OWL ontology:

```sh
robot sssom:xref-extract -i uberon.owl --mapping-file uberon-mappings.sssom.tsv
```

By default, this honours the `oboInOwl:treat-xrefs-as-...` annotations
found in the ontology (contrary to `sssom parse` or `runoak mappings`).

* A command to inject SSSOM-derived axioms into a OWL ontology, with the
  axioms to inject being described by rules written in the
  SSSOM/Transfrorm language.

For example, the following rule can generate bridging axioms between the
_Drosophila Anatomy Ontology_ (FBbt) and the taxon-neutral ontologies
UBERON and CL:
  
```
subject==FBbt:* (object==CL:* || object==UBERON:*) {
    predicate==semapv:crossSpeciesExactMatch -> {
        create_axiom("%subject_id EquivalentTo: %object_id and (BFO:0000050 some NCBITaxon:7227)");
        annotate(%{subject_id}, IAO:00000589, "%{subject_label} (Drosophila)");
    }
}
```

Assuming this is written in a file named `bridge.rules`, one can then
generate a merged ontology between FBbt, UBERON, and CL as follows: 

```sh
robot merge -i uberon.owl -i cl.owl -i fbbt.owl \
      sssom:inject --sssom fbbt-mappings.sssom.tsv \
                   --ruleset bridge.rules \
      annotate --ontology-iri http://purl.obolibrary.org/obo/bridged.owl \
               --output bridged.owl
```

* A command to rename entities within a OWL ontology, similar to the
  builtin command `robot rename` but using a SSSOM mapping set as the
  source of truth for which entity should be renamed and into what.

Building
--------
Build the entire project with:

```sh
$ mvn clean package
```

This will produce four distinct Jar files:

* `sssom-core-x.y.z.jar` (in `core/target`): the core Java library,
  providing the essential parts of the SSSOM implementation with minimal
  external dependencies;
* `sssom-ext-x.y.z.jar` (in `ext/target`): the extended library, built
  on top of `sssom-core` and providing additional features (including
  support for RDF and the SSSOM/T language);
* `sssom-robot-plugin-x.y.z.jar` (in `robot/target`): the ROBOT plugin;
* `sssom-cli-x.y.z.jar` (in `cli/target`): the `sssom-cli` command-line
  tool, as a self-sufficient executable Jar archive.

To re-generate the source files that are derived from the SSSOM LinkML
schema (which should normally not be needed, unless the schema has been
updated), use the `linkml/custom-javagen.py` script (this requires that
the `linkml` package be present in the Python environment):

```sh
$ python linkml/custom-javagen.py
```

To use the library in a Java project, use the following identifiers:

* _group ID_: `org.incenp`;
* _artifact ID_: `sssom-core` for the core library, or `sssom-ext` for
  the extended library (which will bring in `sssom-core` as a
  dependency).

Homepage and repository
-----------------------
The project is located at <https://incenp.org/dvlpt/sssom-java/>, where
some documentation is also hosted. The source code is available in a Git
repository at <https://github.com/gouttegd/sssom-java>.


Copying
-------
SSSOM-Java is distributed under the terms of the GNU General Public
License, version 3 or higher. The full license is included in the
[COPYING file](COPYING) of the source distribution.
