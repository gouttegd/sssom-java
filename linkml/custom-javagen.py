#!/usr/bin/env python3

import os
import click
from linkml.generators.javagen import JavaGenerator
from linkml.utils.generator import shared_arguments
from jinja2 import Template

class_template = """\
package org.incenp.obofoundry.sssom.model;

import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;
{% if cls.name == 'MappingSet' or cls.name == 'Mapping' -%}
import java.util.Map;
import java.util.HashMap;
{% endif -%}
{% if cls.name == 'Mapping' -%}
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Collections;
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
    {%- if f.source_slot.slot_uri %}
    @SlotURI("{{ gen.expand_curie(f.source_slot.slot_uri) }}")
    {%- endif %}
    {%- if f.source_slot.range == 'NonRelativeURI' %}
    @URI
    {%- endif %}
    {%- if gen.get_added_in_version(cls.name, f.source_slot.name) %}
    @Versionable(addedIn = {{ gen.get_added_in_version(cls.name, f.source_slot.name) }})
    {%- endif %}
    {%- if gen.is_slot_constrained(f) %}
    @Setter(AccessLevel.NONE)
    {%- endif %}
    private {% if f.source_slot.range == 'date' %}LocalDate
            {%- elif f.source_slot.range == 'mapping_cardinality_enum' %}MappingCardinality
            {%- elif f.source_slot.range == 'entity_type_enum' %}EntityType
            {%- elif f.source_slot.range == 'predicate_modifier_enum' %}PredicateModifier
            {%- elif f.source_slot.range == 'sssom_version_enum' %}Version
            {%- elif f.source_slot.name == 'curie_map' %}Map<String,String>
            {%- else %}{{ f.range }}{% endif %} {{ f.name }};
{% endfor -%}
{%- for f in gen.get_constrained_slots(cls) %}
    /**
     * Sets the {{ f.source_slot.name }} field to a new value.
     *
     * @param value The new {{ f.source_slot.name }} value to set.
     * @throws IllegalArgumentException If the value is outside of the valid
     *                                  range.
     */
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
{%- for f in cls.fields %}
{%- if f.source_slot.multivalued and f.range == 'List<String>' %}
    /**
     * Gets the list of {{ f.source_slot.name }} values, optionally
     * initializing the list if needed.
     *
     * @param set If {@code true}, the underlying field will be initialized to
     *            an empty list if it happens to be {@code null}.
     * @return The list of {{ f.source_slot.name }} values.
     */
    public {{ f.range }} get{{ f.name[0].upper() }}{{ f.name[1:] }}(boolean set) {
        if ( {{ f.name }} == null && set ) {
            {{ f.name }} = new ArrayList<>();
        }
        return {{ f.name }};
    }
{% endif -%}
{% endfor -%}
{%- if cls.name == 'MappingSet' or cls.name == 'Mapping' %}
    private Map<String,ExtensionValue> extensions;

    /**
     * Gets the map of extension values, optionally initializing the map if
     * needed.
     *
     * @param set If {@code true}, the underlying field will be initialized to
     *            an empty map if it happens to be {@code null}.
     * @return The map of extension values.
     */
    public Map<String,ExtensionValue> getExtensions(boolean set) {
        if ( extensions == null && set ) {
            extensions = new HashMap<>();
        }
        return extensions;
    }
{% endif -%}
{%- if cls.name == 'MappingSet' %}
    /**
     * Gets the prefix map, optionally initializing the map if needed.
     *
     * @param set If {@code true}, the underlying field will be initialized to
     *            an empty map if it happens to be {@code null}.
     * @return The prefix map.
     */
    public Map<String,String> getCurieMap(boolean set) {
        if ( curieMap == null && set ) {
            curieMap = new HashMap<>();
        }
        return curieMap;
    }

    /**
     * Gets the list of mappings, optionally initializing the underlying field
     * to an empty list if needed.
     *
     * @param set If {@code true}, the underlying field will be initialized to
     *            an empty list if it happens to be {@code null}.
     * @return The list of mappings.
     */
    public List<Mapping> getMappings(boolean set) {
        if ( mappings == null && set ) {
            mappings = new ArrayList<>();
        }
        return mappings;
    }

    /**
     * Gets the list of extension definitions, optionally initializing the
     * underlying field to an empty list if needed.
     *
     * @param set If {@code true}, the underlying field will be initialized to
     *            an empty list if it happens to be {@code null}.
     * @return The list of extension definitions.
     */
    public List<ExtensionDefinition> getExtensionDefinitions(boolean set) {
        if ( extensionDefinitions == null && set ) {
            extensionDefinitions = new ArrayList<>();
        }
        return extensionDefinitions;
    }
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
     * Indicates whether this mapping represents a "literal" mapping.
     * <p>
     * A literal mapping is a mapping where either the subject or the object
     * (or both) is a literal, as indicated by the {@code subject_type} or
     * {@code object_type} slot being set to {@link EntityType#RDFS_LITERAL}.
     *
     * @return {@code True} if the mapping is a literal mapping,
     *         {@code false} otherwise.
     */
    public boolean isLiteral() {
        return subjectType == EntityType.RDFS_LITERAL || objectType == EntityType.RDFS_LITERAL;
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
     * Creates a canonical S-expression representing this mapping.
     *
     * @return A String uniquely representing this mapping, as a canonical S-expression.
     */
    public String toSExpr() {
        DecimalFormat floatFormatter = new DecimalFormat("#.###");
        floatFormatter.setRoundingMode(RoundingMode.HALF_UP);

        StringBuilder sb = new StringBuilder();
        sb.append("(7:mapping(");
{%- for f in cls.fields %}
{%- if f.source_slot.name != 'record_id' and f.source_slot.name != 'mapping_cardinality' %}
        if ( {{ f.name }} != null ) {
{%- if f.source_slot.multivalued %}
            sb.append("({{ f.source_slot.name|length }}:{{ f.source_slot.name }}(");
            List<String> tmp = null;
            if ( {{ f.name }}.size() > 1 ) {
                tmp = new ArrayList<>({{ f.name }});
                Collections.sort(tmp);
            } else {
                tmp = {{ f.name }};
            }
            for ( String v : tmp ) {
                sb.append(String.format("%d:%s", v.length(), v));
            }
            sb.append("))");
{%- else %}
{%- if f.range == 'Double' %}
            String v = floatFormatter.format({{ f.name }});
{%- else %}
            String v = String.valueOf({{ f.name }});
{%- endif %}
            sb.append(String.format("({{ f.source_slot.name|length }}:{{ f.source_slot.name }}%d:%s)", v.length(), v));
{%- endif %}
        }
{%- endif %}
{%- endfor %}
        if ( extensions != null ) {
            sb.append("(10:extensions(");
            ArrayList<Map.Entry<String, ExtensionValue>> entries = new ArrayList<>(extensions.entrySet());
            entries.sort((a, b) -> a.getKey().compareTo(b.getKey()));
            for ( Map.Entry<String, ExtensionValue> entry : entries ) {
                String key = entry.getKey();
                String value = entry.getValue().toString();
                sb.append(String.format("(%d:%s%d:%s)", key.length(), key, value.length(), value));
            }
            sb.append("))");
        }
        sb.append("))");
        return sb.toString();
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

    def get_added_in_version(self, class_name, slot_name):
        """Get the added_in annotation carried by this slot, if any.

        :param slot_name: the name of the slot to check
        """

        # Currently the SSSOM LinkML schema only allows to indicate when
        # a brand new slot has been added, but does not cover the case
        # where a pre-existing slot has been added to a class.
        # For example, similarity_measure has existed since before 1.0,
        # but has been added to the MappingSet class only in 1.1 (in 1.0
        # it was only present in the Mapping class) -- we cannot get
        # that information from the model, so we hardcode it in the
        # following dictionary, that we query _before_ querying the
        # schema.
        overrides = {
                "MappingSet:similarity_measure": "1.1",
                "MappingSet:curation_rule": "1.1",
                "MappingSet:curation_rule_text": "1.1"
                }

        added_in = overrides.get(f"{class_name}:{slot_name}", None)
        if added_in is None:
            d = self.schemaview.annotation_dict(slot_name)
            if d is not None:
                added_in = d.get("added_in", None)
        if added_in is not None:
            major, minor = added_in.split(".")
            return f"Version.SSSOM_{major}_{minor}"
        return None

    def expand_curie(self, curie):
        return self.schemaview.expand_curie(curie)

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
    gen.serialize(output_directory, excluded=["Propagatable", "ExtensionDefinition", "Prefix", "NoTermFound", "Versionable"])

if __name__ == "__main__":
    cli()
