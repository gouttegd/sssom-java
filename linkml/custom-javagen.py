#!/usr/bin/env python3

import os
import click
from linkml.generators.javagen import JavaGenerator
from linkml.utils.generator import shared_arguments
from jinja2 import Template

class_template = """\
package org.incenp.obofoundry.sssom.model;

import java.util.List;
import java.time.LocalDate;
{% if cls.name == 'MappingSet' or cls.name == 'Mapping' -%}
import java.util.Map;
{% endif -%}
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
@Data
@EqualsAndHashCode(callSuper=false)
public class {{ cls.name }} {% if cls.is_a -%} extends {{ cls.is_a }} {%- endif %} {
{%- if cls.name == 'MappingSet' %}
    @JsonProperty("curie_map")
    private Map<String,String> curieMap;
{% endif -%}
{%- for f in cls.fields %}
    {%- if f.source_slot.name != f.name %}
    @JsonProperty("{{ f.source_slot.name }}")
    {%- endif %}
    {%- if f.source_slot.range == 'EntityReference' %}
    @EntityReference
    {%- endif %}
    {%- if cls.name == 'MappingSet' and gen.is_propagatable(f.source_slot.name) %}
    @Propagatable
    {%- endif %}
    private {% if f.source_slot.range == 'date' %}LocalDate
            {%- elif f.source_slot.range == 'mapping_cardinality_enum' %}MappingCardinality
            {%- elif f.source_slot.range == 'entity_type_enum' %}EntityType
            {%- elif f.source_slot.range == 'predicate_modifier_enum' %}PredicateModifier
            {%- else %}{{ f.range }}{% endif %} {{ f.name }};
{% endfor -%}
{%- if cls.name == 'MappingSet' %}
    @JsonProperty("extension_definitions")
    private List<ExtensionDefinition> extensionDefinitions;
{% endif -%}
{%- if cls.name == 'MappingSet' or cls.name == 'Mapping' %}
    private Map<String,ExtensionValue> extensions;
{% endif -%}
}

"""

class CustomJavaGenerator(JavaGenerator):
    """
    A custom Java code generator that provides helper methods
    to templates and allows.
    """

    def serialize(self, directory, excluded=[]):
        """Generate the Java code.

        :param directory: output directory where Java files are to be written
        """

        os.makedirs(directory, exist_ok=True)

        template_obj = Template(class_template)
        oodocs = self.create_documents()
        for oodoc in [o for o in oodocs if o.name not in excluded]:
            cls = oodoc.classes[0]
            code = template_obj.render(doc=oodoc, cls=cls, gen=self)

            path = os.path.join(directory, f"{oodoc.name}.java")
            with open(path, "w", encoding="UTF-8") as stream:
                stream.write(code)

    def is_propagatable(self, slot_name):
        """Check if a slot is marked as propagatable.

        :param slot_name: the name of the slot to check
        """

        d = self.schemaview.annotation_dict(slot_name)
        if d is not None:
            return "propagated" in d
        return False


@click.argument("yamlfile", type=click.Path(exists=True, dir_okay=False))
@click.option("--output-directory", default="output", show_default=True)
@click.command()
def cli(yamlfile, output_directory=None):
    gen = CustomJavaGenerator(yamlfile)
    gen.serialize(output_directory, excluded=["Propagatable"])

if __name__ == "__main__":
    cli()
