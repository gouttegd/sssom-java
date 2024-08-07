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
{% if gen.get_constrained_slots(cls) -%}
import lombok.Setter;
{% endif -%}
import com.fasterxml.jackson.annotation.JsonProperty;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(toBuilder = true)
@Data
@EqualsAndHashCode(callSuper=false)
public class {{ cls.name }} {% if cls.is_a -%} extends {{ cls.is_a }} {%- endif %} {
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
    {%- if gen.is_slot_constrained(f) %}
    @Setter(AccessLevel.NONE)
    {%- endif %}
    private {% if f.source_slot.range == 'date' %}LocalDate
            {%- elif f.source_slot.range == 'mapping_cardinality_enum' %}MappingCardinality
            {%- elif f.source_slot.range == 'entity_type_enum' %}EntityType
            {%- elif f.source_slot.range == 'predicate_modifier_enum' %}PredicateModifier
            {%- elif f.source_slot.name == 'curie_map' %}Map<String,String>
            {%- else %}{{ f.range }}{% endif %} {{ f.name }};
{% endfor -%}
{%- for f in gen.get_constrained_slots(cls) %}
    public void set{{ f.name[0].upper() }}{{ f.name[1:] }}({{ f.range }} value) {
        {%- if f.source_slot.maximum_value %}
        if ( value > {{ f.source_slot.maximum_value }} ) {
            throw new IllegalArgumentException("Invalid value for {{ f.source_slot.name }}");
        }
        {%- endif %}
        {%- if f.source_slot.minimum_value is not none %}
        if ( value < {{ f.source_slot.minimum_value }} ) {
            throw new IllegalArgumentException("Invalid value for {{ f.source_slot.name }}");
        }
        {%- endif %}
        {{ f.name }} = value;
    }
{% endfor -%}
{%- if cls.name == 'MappingSet' or cls.name == 'Mapping' %}
    private Map<String,ExtensionValue> extensions;
{% endif -%}
{%- if cls.name == 'Mapping' %}
    /**
     * Indicates whether this mapping represents a "missing" mapping.
     * <p>
     * A missing mapping is a mapping where the <em>subject_id</em>  or the
     * <em>object_id</em> (or both) is the special value
     * <code>sssom:NoTermFound</code>, and indicates that an entity in one
     * domain could not be mapped to any entity in another domain.
     *
     * @return {@code True} if the mapping is a missing mapping,
     *         {@code false} otherwise.
     */
    public boolean isUnmapped() {
        return Constants.NoTermFound.equals(subjectId) || Constants.NoTermFound.equals(objectId);
    }

    /**
     * Creates an inverted version of this mapping with an explicit predicate.
     *
     * @param predicate The predicate to use for the new mapping.
     * @return A new mapping that is the inverse of this one, or {@code null}
     *         if the specified predicate is itself {@code null}.
     */
    public Mapping invert(String predicate) {
        if ( predicate == null ) {
            return null;
        }

        Mapping inverted = toBuilder()
                .predicateId(predicate)
{%- for f in gen.get_slot_suffixes(cls, 'subject') %}
                .subject{{ f }}(object{{ f }})
                .object{{ f }}(subject{{ f }})
{%- endfor -%}
        .build();

        if ( mappingCardinality != null ) {
            inverted.mappingCardinality = mappingCardinality.getInverse();
        }

        return inverted;
    }

    /**
     * Creates an inverted version of this mapping if possible.
     * <p>
     * Inversion is possible if the predicate is one of the known "common"
     * predicates and is invertible. To invert a mapping with an arbitrary
     * predicate, use {@link #invert(String)}.
     *
     * @return A new mapping that is the inverse of this one, or {@code null}
     *         if inversion is not possible.
     */
    public Mapping invert() {
        CommonPredicate predicate = CommonPredicate.fromString(predicateId);
        if ( predicate == null || !predicate.isInvertible() ) {
            return null;
        }

        return invert(predicate.getInverse());
    }

    /**
     * @deprecated Use {@code #getSimilarityScore()} instead.
     */
    @Deprecated
    public Double getSemanticSimilarityScore() {
        return getSimilarityScore();
    }

    /**
     * @deprecated Use {@code #setSimilarityScore(Double)} instead.
     */
    @Deprecated
    public void setSemanticSimilarityScore(Double value) {
        setSimilarityScore(value);
    }

    /**
     * @deprecated Use {@code #getSimilarityMeasure()} instead.
     */
    @Deprecated
    public String getSemanticSimilarityMeasure() {
        return getSimilarityMeasure();
    }

    /**
     * @deprecated Use {@code #setSimilarityMeasure(String)} instead.
     */
    @Deprecated
    public void setSemanticSimilarityMeasure(String value) {
        setSimilarityMeasure(value);
    }
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

    def is_slot_constrained(self, slot):
        """Check if the slot has special constraints.

        :param slot: the slot to check for constraints
        """

        return slot.source_slot.minimum_value is not None or slot.source_slot.maximum_value is not None

    def get_constrained_slots(self, cls):
        """Get all slots in a class that have special constraints.

        :param cls: the class to query for constrained slots
        """

        return [f for f in cls.fields if self.is_slot_constrained(f)];

    def get_slot_suffixes(self, cls, prefix):
        """Get the suffixes of all slots that start with the given prefix.

        :param cls: the class to query for slot suffixes.
        :param prefix: the prefix to look for in slot names.
        """
        
        n = len(prefix)
        return [f.name[n:] for f in cls.fields if f.name.startswith(prefix)]


@click.argument("yamlfile", type=click.Path(exists=True, dir_okay=False))
@click.option("--output-directory", default="output", show_default=True)
@click.command()
def cli(yamlfile, output_directory=None):
    gen = CustomJavaGenerator(yamlfile)
    gen.serialize(output_directory, excluded=["Propagatable", "ExtensionDefinition", "Prefix"])

if __name__ == "__main__":
    cli()
