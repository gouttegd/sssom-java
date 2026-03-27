#!/usr/bin/env python3

import os
import click
from linkml.generators.javagen import JavaGenerator
from linkml.utils.generator import shared_arguments
from jinja2 import Template
from jsonasobj2 import as_dict

custom_types = {
    "date": "LocalDate",
    "mapping_cardinality_enum": "MappingCardinality",
    "entity_type_enum": "EntityType",
    "predicate_modifier_enum": "PredicateModifier",
    "sssom_version_enum": "Version",
    "NonRelativeURI": "String",
}

class_template = """\
package org.incenp.obofoundry.sssom.model;

import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;
{% if cls.name == 'Mapping' -%}
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Collections;

{% else %}
{% endif -%}
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a <code>{{ cls.source_class.name }}</code> object.
 * <p>
 * Automatically generated from the SSSOM LinkML schema.
 */
public class {{ cls.name }} {% if cls.is_a -%} extends {{ cls.is_a }} {%- endif %} {

{#- Fields declarations. #}
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
    @SlotURI("{{ f.slot_uri }}")
    {%- endif %}
    {%- if f.source_slot.range == 'NonRelativeURI' %}
    @URI
    {%- endif %}
    {%- if gen.get_added_in_version(f.source_slot.name, cls.source_class.name) %}
    @Versionable(addedIn = {{ gen.get_added_in_version(f.source_slot.name, cls.source_class.name) }})
    {%- endif %}
    private {{ gen.get_range(f) }} {{ f.name }};
{% endfor -%}

{#- Custom extension field. #}
    private Map<String,ExtensionValue> extensions;

{#- Constructors. #}

    /**
     * Creates a new empty instance.
     */
    public {{ cls.name }}() {
    }

    /**
     * Creates a new instance from the specified values.
     */
    protected {{ cls.name }}({% for f in cls.fields %}final {{ gen.get_range(f) }} {{ f.name }},
            {% endfor %}final Map<String,ExtensionValue> extensions) {
{%- for f in cls.fields %}
        this.{{ f.name }} = {{ f.name }};{% endfor %}
        this.extensions = extensions;
    }

{#- Accessors. #}
{% for f in cls.fields %}
    /**
     * Gets the value of the <code>{{ f.source_slot.name }}</code> slot.
     */
    public {{ gen.get_range(f) }} {{ gen.get_read_accessor_name(f.source_slot) }}() {
        return this.{{ f.name }};
    }

{%- if f.source_slot.name == "curie_map" %}

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
{%- elif f.source_slot.multivalued %}

    /**
     * Gets the list of <code>{{ f.source_slot.name }}</code> values, optionally
     * initializing the list if needed.
     *
     * @param set If {@code true}, the underlying field will be initialized to
     *            an empty list if it happens to be {@code null}.
     * @return The list of {{ f.source_slot.name }} values.
     */
    public {{ gen.get_range(f) }} {{ gen.get_read_accessor_name(f.source_slot) }}(boolean set) {
        if ( {{ f.name }} == null && set ) {
            {{ f.name }} = new ArrayList<>();
        }
        return {{ f.name }};
    }
{%- endif %}

    /**
     * Sets the value of the <code>{{ f.source_slot.name }}</code> slot.
     */
    public void {{ gen.get_write_accessor_name(f.source_slot) }}(final {{ gen.get_range(f) }} value) {
        {%- if f.source_slot.maximum_value is not none %}
        if ( value > {{ f.source_slot.maximum_value }} ) {
            throw new IllegalArgumentException("Invalid value for {{ f.source_slot.name }}");
        }
        {%- endif %}
        {%- if f.source_slot.minimum_value is not none %}
        if ( value < {{ f.source_slot.minimum_value }} ) {
            throw new IllegalArgumentException("Invalid value for {{ f.source_slot.name }}");
        }
        {%- endif %}
        this.{{ f.name }} = value;
    }
{% endfor -%}

{# Additional accessors for the extension slot. #}
    /**
     * Gets the map of extension values.
     */
    public Map<String,ExtensionValue> getExtensions() {
        return extensions;
    }

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

    /**
     * Sets the map of extension values.
     */
    public void setExtensions(final Map<String,ExtensionValue> value) {
        this.extensions = value;
    }

{#- #}
{% if cls.name == 'Mapping' %}
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
     * Indicates whether the object of this mapping is not mapped to any
     * entity on the subject side.
     *
     * @return {@code True} if the subject of this mapping is
     *         <code>sssom:NoTermFound</code>, {@code false} otherwise.
     */
    public boolean hasUnmappedSubject() {
        return Constants.NoTermFound.equals(subjectId);
    }

    /**
     * Indicates whether the subject of this mapping is not mapped to any
     * entity on the object side.
     *
     * @return {@code True} if the object of this mapping is
     *         <code>sssom:NoTermFound</code>, {@code false} otherwise.
     */
    public boolean hasUnmappedObject() {
        return Constants.NoTermFound.equals(objectId);
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

{#- The overridden methods from Object. #}
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{{ cls.name }}(");
{%- for f in cls.fields %}
{%-   if f.range in ["int", "float", "double", "boolean"] %}
        sb.append("{{ gen.get_slot_actual_name(f.source_slot) }}=");
        sb.append(this.{{ f.name }});
        sb.append(",");
{%-   else %}
 {%-  endif %}
        if ( this.{{ f.name }} != null ) {
            sb.append("{{ gen.get_slot_actual_name(f.source_slot) }}=");
            sb.append(this.{{ f.name }});
            sb.append(",");
        }
{%- endfor %}
        if ( extensions != null && !extensions.isEmpty() ) {
            sb.append("extensions={");
            for ( Map.Entry<String, ExtensionValue> entry : extensions.entrySet() ) {
                sb.append(entry.getKey());
                sb.append("=");
                sb.append(entry.getValue());
                sb.append(",");
            }
            sb.append("}");
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if ( o == this ) return true;
        if ( !(o instanceof {{ cls.name }}) ) return false;
        final {{ cls.name }} other = ({{ cls.name }}) o;
        if ( !other.canEqual((Object) this)) return false;
{#- No need to call the parent's method if we are directly derived from Object. #}
{%- if cls.is_a %}
        if ( !super.equals(o) ) return false;
{% endif %}
{#- Now compare every slots. #}
{%- for f in cls.fields %}
{%-   if f.range in ["int", "float", "double", "boolean"] %}
        if ( this.{{ f.name }} != other.{{ f.name }} ) return false;
{%-   else %}
        if ( this.{{ f.name }} == null ? other.{{ f.name }} != null : !this.{{ f.name }}.equals(other.{{ f.name }})) return false;
 {%-  endif %}
{%- endfor %}
        if ( this.extensions == null ? other.extensions != null : !this.extensions.equals(other.extensions)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof {{ cls.name }};
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = {% if cls.is_a %}super.hashCode(){% else %}1{% endif %};
{%- for f in cls.fields %}
{%-   if f.range == "int" %}
        result = result * PRIME + this.{{ f.name }};
{%-   elif f.range == "float" %}
        result = result * PRIME + Float.floatToIntBits(this.{{ f.name }});
{%-   elif f.range == "double" %}
        final long ${{ f.name }} = Double.doubleToLongBits(this.{{ f.name }});
        result = result * PRIME + (int) (${{ f.name }} >>> 32 ^ ${{ f.name }});
{%-   elif f.range == "boolean" %}
        result = result * PRIME + (this.{{ f.name }} ? 79 : 97);
{%-   else %}
        result = result * PRIME + (this.{{ f.name }} == null ? 43 : this.{{ f.name }}.hashCode());
{%-   endif %}
{%- endfor %}
        result = result * PRIME + (this.extensions == null ? 43 : this.extensions.hashCode());
        return result;
    }

{#- Builder object. #}

    public static class {{ cls.name }}Builder {
{%- for f in cls.fields %}
        private {{ gen.get_range(f) }} {{ f.name }};{% endfor -%}
{##}
        private Map<String,ExtensionValue> extensions;

        {{ cls.name }}Builder() {
        }
{##}
{%- for f in cls.fields %}
        public {{ cls.name }}.{{ cls.name }}Builder {{ f.name }}(final {{ gen.get_range(f) }} {{ f.name }}) {
            this.{{ f.name }} = {{ f.name }};
            return this;
        }
{% endfor -%}
{##}
        public {{ cls.name }}.{{ cls.name}}Builder extensions(final Map<String,ExtensionValue> extensions) {
            this.extensions = extensions;
            return this;
        }

        public {{ cls.name }} build() {
            return new {{ cls.name }}({% for f in cls.fields %}this.{{ f.name }},
                {% endfor %}this.extensions);
        }

        public String toString() {
            return "{{ cls.name }}.{{ cls.name }}Builder({% for f in cls.fields %}{{ f.name }}=" + this.{{ f.name }}
                + ", {% endfor %}extensions=" + this.extensions + ")";
        }
    }

    public static {{ cls.name }}.{{ cls.name }}Builder builder() {
        return new {{ cls.name }}.{{ cls.name}}Builder();
    }

    public {{ cls.name }}.{{ cls.name }}Builder toBuilder() {
        return new {{ cls.name }}.{{ cls.name }}Builder(){% for f in cls.fields %}
            .{{ f.name }}(this.{{ f.name }}){% endfor %}
            .extensions(this.extensions);
    }
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

    def get_added_in_version(self, slot_name, class_name):
        """Get the added_in annotation carried by this slot, if any.

        :param slot_name: the name of the slot to check
        :param class_name: the name of the class the slot belongs to
        """

        induced_slot = self.schemaview.induced_slot(slot_name, class_name)
        added_in = as_dict(induced_slot.annotations).get("added_in", None)
        if added_in is not None:
            major, minor = added_in.get("value").split(".")
            return f"Version.SSSOM_{major}_{minor}"
        return None

    def get_slot_suffixes(self, cls, prefix):
        """Get the suffixes of all slots that start with the given prefix.

        :param cls: the class to query for slot suffixes.
        :param prefix: the prefix to look for in slot names.
        """

        n = len(prefix)
        return [f.name[n:] for f in cls.fields if f.name.startswith(prefix)]

    def get_range(self, field):
        """Get the real range for a given field.

        :param field: The field object produced by the code generator.
        """
        custom = custom_types.get(field.source_slot.range, None)
        if custom:
            if field.source_slot.multivalued:
                return f"List<{custom}>"
            else:
                return custom
        elif field.source_slot.name == "curie_map":
            return "Map<String,String>"
        else:
            return field.range


@click.argument("yamlfile", type=click.Path(exists=True, dir_okay=False))
@click.option("--output-directory", default="output", show_default=True)
@click.command()
def cli(yamlfile, output_directory=None):
    gen = CustomJavaGenerator(yamlfile)
    gen.serialize(output_directory, excluded=["Propagatable", "ExtensionDefinition", "Prefix", "NoTermFound", "Versionable"])

if __name__ == "__main__":
    cli()
