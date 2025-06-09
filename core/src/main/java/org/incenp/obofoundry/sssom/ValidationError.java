/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2025 Damien Goutte-Gattat
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

import java.util.EnumSet;
import java.util.HashSet;

/**
 * Represents all the post-parsing errors that can make a mapping set invalid.
 */
public enum ValidationError {
    MISSING_SET_ID("Missing set ID", true),
    MISSING_LICENSE("Missing license", true),
    REDEFINED_BUILTIN_PREFIX("Re-defined builtin prefix in the provided curie map", true),
    MISSING_SUBJECT("Missing subject"),
    MISSING_OBJECT("Missing object"),
    MISSING_PREDICATE("Missing predicate"),
    MISSING_JUSTIFICATION("Missing justification"),
    INVALID_PREDICATE_TYPE("Invalid predicate type");

    private String msg;
    private boolean setLevel;

    ValidationError(String msg) {
        this.msg = msg;
        setLevel = false;
    }

    ValidationError(String msg, boolean setLevel) {
        this.msg = msg;
        this.setLevel = setLevel;
    }

    /**
     * Gets the human-readable error message for this error.
     * 
     * @return The error message.
     */
    public String getMessage() {
        return msg;
    }

    /**
     * Gets a human-readable error message for a set of validation errors.
     * 
     * @param values A set of validation errors.
     * @return An error message listing all the individual errors.
     */
    public static String getMessage(EnumSet<ValidationError> values) {
        HashSet<String> msg = new HashSet<>();
        for ( ValidationError error : values ) {
            if ( error.setLevel ) {
                msg.add(error.msg);
            }
        }
        if ( !msg.isEmpty() ) {
            return "Invalid mapping set: " + String.join(", ", msg);
        } else {
            for ( ValidationError error : values ) {
                msg.add(error.msg);
            }
            return "Invalid mapping: " + String.join(", ", msg);
        }
    }
}
