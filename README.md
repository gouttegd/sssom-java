[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.8192579.svg)](https://doi.org/10.5281/zenodo.8192579)


SSSOM Java - SSSOM library for Java
===================================

SSSOM-Java is an implementation of the [Simple Standard for Sharing
Ontology Mappings
(SSSOM)](https://mapping-commons.github.io/sssom/home/) specification
for the Java language – just a hobby, won’t be big and professional like
`sssom-py`.

The project provides a Java library that can be used to support the
SSSOM standard in a Java application, a program to manipulate mapping
sets from the command line, and a pluggable command for the
[ROBOT](http://robot.obolibrary.org/) ontology manipulation tool.

Features
--------
* Reading a SSSOM mapping set from the TSV serialisation format.

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

* Writing a SSSOM mapping set to the TSV serialisation format.

```java
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.incenp.obofoundry.sssom.TSVWriter;

[...]
MappingSet mappingSet = ...;
try {
    TSVWriter writer = new TSVWriter("my-mappings.sssom.tsv");
    writer.write(mappingSet);
} catch (IOException e) {
    // I/O error
}
```

* Reading/writing a mapping set from/to the JSON serialisation format.

* Reading/writing a mapping set from/to the RDF/Turtle serialisation
  format.

* Extracting SSSOM mappings from a OWL ontology with ROBOT:

```sh
robot sssom:xref-extract -i uberon.owl --mapping-file uberon-mappings.sssom.tsv
```

By default, this honours the `oboInOwl:treat-xrefs-as-...` annotations
found in the ontology (contrary to `sssom parse` or `runoak mappings`).

* Injecting arbitrary axioms into a OWL ontology with ROBOT, with the
  axioms to inject being described by rules written in an ad-hoc
  [SSSOM/Transform language](https://incenp.org/dvlpt/sssom-java/sssom-transform.html).
  As an example, here is how to use it to generate bridging axioms
  between the _Drosophila Anatomy Ontology_ (FBbt) and the taxon-neutral
  ontologies UBERON and CL:
  
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

* Renaming entities within a OWL ontology, using a SSSOM mapping set as
the source of truth for which entity should be renamed and into what.

* Manipulating mapping sets from the command line, with a dedicated
  command-line tool named `sssom-cli`.

Building
--------
Build by running `mvn clean package`. This will produce five distinct
Jar files:

* `sssom-core-x.y.z.jar` (in `core/target`): a minimal Java library,
  containing only the classes implementing the SSSOM specification.
* `sssom-ext-x.y.z.jar` (in `ext/target`): an extended library, built
  on top of `sssom-core` and providing more classes to facilitate the
  manipulation of mappings.
* `sssom-robot-plugin-x.y.z.jar` (in `robot/target`): a file usable as a
  ROBOT plugin.
* `sssom-robot-standalone-x.y.z.jar` (in `robot/target`): a standalone
  version of ROBOT (1.9.6), which includes the command(s) from the SSSOM
  plugin as if they were built-in commands.
* `sssom-cli-x.y.z.jar` (in `cli/target`): the `sssom-cli` command-line
  tool, as a self-sufficient executable Jar archive.

To use the library in a Java project, use the following identifiers:

* _group ID_: `org.incenp`;
* _artifact ID_: `sssom-core` for the core library, or `sssom-ext` for
  extended library (which will bring in `sssom-core` as a dependency).

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

SSSOM-Java includes code that is automatically derived from the [SSSOM
schema](https://github.com/mapping-commons/sssom/) (all classes under the
`org.incenp.obofoundry.sssom.model` namespace). That code is distributed
under the same terms as the schema itself:

> Copyright (c) 2022, Nico Matentzoglu
> All rights reserved.
>
> Redistribution and use in source and binary forms, with or without
> modification, are permitted provided that the following conditions are
> met:
>
> 1. Redistributions of source code must retain the above copyright
>    notice, this list of conditions and the following disclaimer.
>
> 2. Redistributions in binary form must reproduce the above copyright
>    notice, this list of conditions and the following disclaimer in the
>    documentation and/or other materials provided with the
>    distribution.
>
> 3. Neither the name of the copyright holder nor the names of its
>    contributors may be used to endorse or promote products derived
>    from this software without specific prior written permission.
>
> THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
> "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
> LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
> A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
> HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
> SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
> LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
> DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
> THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
> (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
> OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
