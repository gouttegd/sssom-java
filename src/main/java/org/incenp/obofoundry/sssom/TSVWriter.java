/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2023 Damien Goutte-Gattat
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the Gnu General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.incenp.obofoundry.sssom;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.incenp.obofoundry.sssom.model.EntityReference;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.model.MappingSet;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A writer to serialise a SSSOM mapping set into the TSV format. For now, only
 * the “embedded metadata” variant is supported.
 * <p>
 * If the mapping set has a CURIE map ({@link MappingSet#getCurieMap()}), it is
 * automatically used to shorten identifiers when they are written to the file.
 * <p>
 * Usage:
 * 
 * <pre>
 * MappingSet mappingSet = ...;
 * try {
 *     TSVWriter writer = new TSVWriter("my-mappings.sssom.tsv");
 *     writer.write(mappingSet);
 *     writer.close();
 * } catch ( IOException ioe ) {
 *     // Generic I/O error
 * }
 * </pre>
 */
public class TSVWriter {

    private BufferedWriter writer;
    private PrefixManager prefixManager = new PrefixManager();

    /**
     * Creates a new instance that will write data to the specified file.
     * 
     * @param file The file to write to.
     * @throws IOException If the file cannot be opened for any reason.
     */
    public TSVWriter(File file) throws IOException {
        writer = new BufferedWriter(new FileWriter(file));
    }

    /**
     * Creates a new instance that will write data to a file with the specified
     * filename.
     * 
     * @param filename The name of the file to write to.
     * @throws IOException If the file cannot be opened for any reason.
     */
    public TSVWriter(String filename) throws IOException {
        this(new File(filename));
    }

    /**
     * Closes the underlying file.
     * 
     * @throws IOException If an I/O error occurs.
     */
    public void close() throws IOException {
        writer.close();
    }

    /**
     * Serialises a mapping set into the underlying file.
     * 
     * @param mappingSet The mapping set to serialise.
     * @throws IOException If an I/O error occurs.
     */
    public void write(MappingSet mappingSet) throws IOException {
        prefixManager.add(mappingSet.getCurieMap());

        // Write the metadata
        // FIXME: Support writing them in a separate file
        List<String> metadata = renderMetadata(mappingSet);
        for ( String s : metadata ) {
            writer.append('#');
            writer.append(s);
            writer.append('\n');
        }

        // Write the column headers
        List<Field> fields = findUsedFields(mappingSet);
        for ( int i = 0, n = fields.size(); i < n; i++ ) {
            Field field = fields.get(i);
            String json_name = field.getName();
            JsonProperty jsonAnnot = field.getDeclaredAnnotation(JsonProperty.class);
            if ( jsonAnnot != null ) {
                json_name = jsonAnnot.value();
            }

            writer.append(json_name);
            if ( i < n - 1 ) {
                writer.append('\t');
            }
        }
        writer.append('\n');

        // Write the individual mappings
        for ( Mapping mapping : mappingSet.getMappings() ) {
            for ( int i = 0, n = fields.size(); i < n; i++ ) {
                Field field = fields.get(i);
                Object value = getValue(mapping, field.getName());
                if ( value != null ) {
                    writer.append(renderValue(field, value));
                }

                if ( i < n - 1 ) {
                    writer.append('\t');
                }
            }

            writer.append('\n');
        }
    }

    /*
     * Translate a single value from a mapping into a string.
     */
    private String renderValue(Field field, Object value) {
        Type fieldType = field.getType();
        boolean isEntityReference = field.isAnnotationPresent(EntityReference.class);

        if ( fieldType.equals(String.class) ) {
            if ( isEntityReference ) {
                return prefixManager.shortenIdentifier(String.class.cast(value));
            } else {
                return value.toString();
            }
        } else if ( fieldType.equals(List.class) ) {
            @SuppressWarnings("unchecked")
            List<String> list = List.class.cast(value);
            if ( isEntityReference ) {
                list = prefixManager.shortenIdentifiers(list, false);
            }
            return String.join("|", list);
        } else if ( fieldType.equals(Double.class) ) {
            return String.format("%f", value);
        } else if ( fieldType.equals(LocalDateTime.class) ) {
            LocalDateTime date = LocalDateTime.class.cast(value);
            return date.format(DateTimeFormatter.ISO_DATE);
        }
        return "";
    }

    /*
     * Translate the metadata of a mapping set into a list of strings.
     */
    private List<String> renderMetadata(MappingSet mappingSet) {
        List<String> metadata = new ArrayList<String>();

        Field[] fields = mappingSet.getClass().getDeclaredFields();
        // Metadata elements SHOULD appear by alphabetical order
        Arrays.sort(fields, (f1, f2) -> String.CASE_INSENSITIVE_ORDER.compare(f1.getName(), f2.getName()));

        for ( Field field : fields ) {
            if ( field.getName().equals("mappings") ) {
                // For now we are dealing with the metadata only, ignore the mappings
                continue;
            }

            Object value = getValue(mappingSet, field.getName());
            if ( value == null ) {
                continue;
            }

            String json_name = field.getName();
            JsonProperty jsonAnnot = field.getDeclaredAnnotation(JsonProperty.class);
            if ( jsonAnnot != null ) {
                json_name = jsonAnnot.value();
            }

            boolean isEntityReference = field.isAnnotationPresent(EntityReference.class);

            Type fieldType = field.getType();
            if ( fieldType.equals(String.class) ) {
                String text = String.class.cast(value);
                if ( isEntityReference ) {
                    text = prefixManager.shortenIdentifier(text);
                }
                metadata.add(String.format("%s: \"%s\"", json_name, text));
            } else if ( fieldType.equals(List.class) ) {
                @SuppressWarnings("unchecked")
                List<String> list = List.class.cast(value);
                if ( list.size() > 0 ) {
                    metadata.add(json_name + ":");
                    for ( String text : list ) {
                        if ( isEntityReference ) {
                            text = prefixManager.shortenIdentifier(text);
                        }
                        metadata.add(String.format("  - \"%s\"", text));
                    }
                }
            } else if ( fieldType.equals(Map.class) ) {
                @SuppressWarnings("unchecked")
                Map<String, String> map = Map.class.cast(value);
                if ( map.size() > 0 ) {
                    metadata.add(json_name + ":");
                    for ( String key : map.keySet() ) {
                        metadata.add(String.format("  %s: \"%s\"", key, map.get(key)));
                    }
                }
            } else if ( fieldType.equals(LocalDateTime.class) ) {
                LocalDateTime v = LocalDateTime.class.cast(value);
                // The SSSOM specification says nothing on how to serialise dates, but LinkML
                // says “for xsd dates, datetimes, and times, AtomicValue must be a string
                // conforming to the relevant ISO type”. I assume this means ISO-8601.
                metadata.add(String.format("%s: \"%s\"", json_name, v.format(DateTimeFormatter.ISO_DATE)));
            }
        }

        return metadata;
    }

    /*
     * Figure out which fields we need to write.
     */
    private List<Field> findUsedFields(MappingSet mappingSet) {
        Set<Field> usedFields = new HashSet<Field>();
        Field[] fields = Mapping.class.getDeclaredFields();
        for ( Mapping mapping : mappingSet.getMappings() ) {
            for ( Field field : fields ) {
                Object value = getValue(mapping, field.getName());
                if ( value != null ) {
                    usedFields.add(field);
                }
            }
        }

        List<Field> sortedFields = new ArrayList<Field>();
        // FIXME: This relies on Java reflection giving us the fields in the order in
        // which they are declared. This is not guaranteed.
        for ( Field field : fields ) {
            if ( usedFields.contains(field) ) {
                sortedFields.add(field);
            }
        }

        return sortedFields;
    }

    /*
     * Helper method to get the value of a field on a given object through
     * reflection.
     */
    private Object getValue(Object object, String fieldName) {
        String accessorName = String.format("get%c%s", Character.toUpperCase(fieldName.charAt(0)),
                fieldName.substring(1));
        try {
            Method accessor = object.getClass().getDeclaredMethod(accessorName, (Class<?>[]) null);
            return accessor.invoke(object, (Object[]) null);
        } catch ( NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e ) {
            // Should never happen, or something went very bad
        }

        return null;
    }
}
