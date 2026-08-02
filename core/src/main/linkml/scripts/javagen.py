#!/usr/bin/env python3

import click
from pathlib import Path
from linkml.generators.javagen import JavaBundle, JavaGenerator
from jsonasobj2 import as_dict

custom_types = {
    "date": "LocalDate",
    "mapping_cardinality_enum": "MappingCardinality",
    "entity_type_enum": "EntityType",
    "predicate_modifier_enum": "PredicateModifier",
    "sssom_version_enum": "Version",
    "NonRelativeURI": "String",
}


class CustomJavaGenerator(JavaGenerator):
    """
    A custom Java code generator tailored for the needs of SSSOM-Java.

    SSSOM-Java was started long before the LinkML-Java runtime, and is
    therefore completely independent of it and has its own requirements
    about the generated code.

    Compared to the upstream generator in LinkML-Py, this generator:

    (1) allows to exclude some classes from the generation;
    (2) gives access to some annotations (to figure out whether a given
        slot is propagatable and when it was added to the schema);
    (3) allows to figure out whether a given slot pertains to the
        or the object of a mapping;
    (4) allows to apply a custom map from LinkML ranges to Java types.

    (1) and (2) should probably be generalized and upstreamed at some
    point. (4) could also be upstreamed but there's an argument to be
    made that subclassing the generator (as we do here) is in fact the
    appropriate method of doing such customisations. (3) is completely
    SSSOM-specific.
    """

    def render(self, excluded=[]):
        """Generates the Java bundle.

        :param excluded: list of class names to exclude from generation
        """
        files = {}
        for oodoc in self.create_documents():
            if not oodoc.classes or oodoc.name in excluded:
                continue
            template = self.template_cache.get_template(oodoc.name, "class")
            code = template.render(doc=oodoc, cls=oodoc.classes[0], gen=self)
            files[f"{oodoc.name}.java"] = code

        return JavaBundle(files=files, package=self.package)

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


@click.option("--output-directory",
              type=click.Path(dir_okay=True, file_okay=False),
              default=Path("core/src/main/java"))
@click.option("--linkml-directory",
              type=click.Path(dir_okay=True, file_okay=False, exists=True),
              default=Path("core/src/main/linkml/schemas"))
@click.option("--templates-directory",
              type=click.Path(dir_okay=True, file_okay=False, exists=True),
              default=Path("core/src/main/linkml/templates"))
@click.command()
def cli(linkml_directory, output_directory, templates_directory):

    excluded = ["Propagatable", "ExtensionDefinition", "Prefix",
                "NoTermFound", "Versionable"]

    for schema in linkml_directory.glob("**/*.yaml"):
        package_dir = schema.relative_to(linkml_directory).parent
        output_dir = output_directory / package_dir
        output_dir.mkdir(parents=True, exist_ok=True)

        package_name = package_dir.as_posix().replace("/", ".")
        gen = CustomJavaGenerator(schema,
                                  template_dir=templates_directory,
                                  package=package_name)
        bundle = gen.render(excluded=excluded)
        gen.serialize(output_dir, rendered_module=bundle)


if __name__ == "__main__":
    cli()
