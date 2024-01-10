/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2024 Damien Goutte-Gattat
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

package org.incenp.obofoundry.sssom.model;

/**
 * Represents the definition of an extension slot.
 */
public class ExtensionDefinition {
    private String slotName;

    private String property;

    private String typeHint;

    private ValueType realType;

    /**
     * Creates a new instance with the default type.
     * 
     * @param slotName The name of the extension slot.
     * @param property The property associated with the extension slot.
     */
    public ExtensionDefinition(String slotName, String property) {
        this.slotName = slotName;
        this.property = property;
        this.typeHint = ValueType.STRING.toString();
        this.realType = ValueType.STRING;
    }

    /**
     * Creates a new instance with the specified type hint.
     * 
     * @param slotName The name of the extension slot.
     * @param property The property associated with the extension slot.
     * @param typeHint The type of the extension slot.
     */
    public ExtensionDefinition(String slotName, String property, String typeHint) {
        this.slotName = slotName;
        this.property = property;

        if ( typeHint == null ) {
            typeHint = ValueType.STRING.toString();
        }
        this.typeHint = typeHint;
        this.realType = ValueType.fromIRI(typeHint);
    }

    /**
     * @return The name of the extension slot.
     */
    public String getSlotName() {
        return slotName;
    }

    /**
     * @return The property associated with the extension slot.
     */
    public String getProperty() {
        return property;
    }

    /**
     * @return The declared type of the extension slot.
     */
    public String getTypeHint() {
        return typeHint;
    }

    /**
     * @return The actual type used for this extension slot.
     */
    public ValueType getEffectiveType() {
        return realType;
    }

    /**
     * Sets the type hint.
     * 
     * @param hint The new type hint to assign (if {@code null}, defaults to
     *             {@code xsd:string}).
     */
    public void setTypeHint(String hint) {
        if ( hint == null ) {
            typeHint = ValueType.STRING.toString();
            realType = ValueType.STRING;
        } else {
            typeHint = hint;
            realType = ValueType.fromIRI(hint);
        }
    }

    /**
     * Sets the slot name.
     * 
     * @param name The new slot name to assign.
     */
    public void setSlotName(String name) {
        slotName = name;
    }
}
