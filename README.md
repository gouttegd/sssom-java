[![DOI](https://zenodo.org/badge/651692695.svg)](https://zenodo.org/badge/latestdoi/651692695)

SSSOM Java - SSSOM library for Java
===================================

SSSOM-Java is an implementation of the [Simple Standard for Sharing
Ontology Mappings
(SSSOM)](https://mapping-commons.github.io/sssom/home/) specification
for the Java language – just a hobby, won’t be big and professional like
`sssom-py`.

What is supported?
------------------
* Reading a SSSOM mapping set from the TSV serialisation format (both
  internal and external metadata variants are supported).

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

* Writing a SSSOM mapping set to the TSV serialisation format (only the
  internal metadata variant is supported).

```java
import org.incenp.obofoundry.sssom.model.MappingSet;
import org.incenp.obofoundry.sssom.TSVWriter;

[...]
MappingSet mappingSet = ...;
try {
    TSVWriter writer = new TSVWriter("my-mappings.sssom.tsv");
    writer.write(mappingSet);
    writer.close();
} catch (IOException e) {
    // I/O error
}
```

* Injecting arbitrary axioms into a OWL ontology with ROBOT, with the
  axioms to inject being described by rules written in an ad-hoc
  _SSSOM/Transform_ language. That language still needs to be described
  somewhere, but as an example, here is how to use it to generate
  bridging axioms between the _Drosophila Anatomy Ontology_ (FBbt) and
  the taxon-neutral ontologies UBERON and CL:
  
```
subject==FBbt:* (object==CL:* || object==UBERON:*) {
    predicate==semapv:crossSpeciesExactMatch -> {
        gen('%subject_id EquivalentTo %object_id and (BFO:0000050 some NCBITaxon:7227)');
        gen('%subject_id Annotation IAO:00000589 "%subject_label (Drosophila)"');
    }
}
```

Assuming this is written in a file named `bridge.rules`, one can then
generate a merged ontology between FBbt, UBERON, and CL as follows: 

```sh
robot merge -i uberon.owl -i cl.owl -i fbbt.owl \
      sssom-inject --sssom fbbt-mappings.sssom.tsv \
                   --ruleset bridges.rules \
      annotate --ontology-iri http://purl.obolibrary.org/obo/bridged.owl \
               --output bridged.owl
```

Building
--------
Build by running `mvn clean package`. This will produce three distinct
Jar files:

* `sssom-java-x.y.z.jar`: a minimal Java library, containing only the
  classes from this project proper (dependencies _not_ included).
* `sssom-robot-plugin-x.y.z.jar`: a file usable as a ROBOT plugin, once
  ROBOT supports such plugins.
* `sssom-robot-standalone-x.y.z.jar`: a standalone version of ROBOT
  (1.9.4), which includes the command(s) from the SSSOM plugin as if
  they were built-in commands.

Todo
----
* Support for other serialisation formats (RDF/XML-serialised OWL
  axioms, JSON), reading and writing.
* Support for mapping inversion.
* More documentation (particulary for SSSOM/Transform).
* Support for ”direct” translation of mappings into OWL axioms, as per
  the SSSOM specification (without using custom rules).


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
