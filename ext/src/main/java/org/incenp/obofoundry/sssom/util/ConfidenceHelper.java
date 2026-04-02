/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2023,2024,2025 Damien Goutte-Gattat
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

package org.incenp.obofoundry.sssom.util;

import org.incenp.obofoundry.sssom.model.Mapping;

/**
 * A helper class to work with confidence values and estimations.
 */
public class ConfidenceHelper {

    /**
     * Computes the aggregated confidence value for a mapping.
     * <p>
     * The aggregated confidence is a single value that factors in both the
     * confidence of the original creator of the mapping record and the confidence
     * of the reviewer of the record.
     * 
     * @see <a href="https://mapping-commons.github.io/sssom/confidence-model/">How
     *      to assess confidence</a>
     * @param mapping The mapping for which to compute the aggregated confidence.
     * @return The aggregated confidence value.
     */
    public double aggregate(Mapping mapping) {
        double c = mapping.getConfidence() != null ? mapping.getConfidence().doubleValue() : 1.0;
        double r = mapping.getReviewerAgreement() != null ? mapping.getReviewerAgreement().doubleValue() : 1.0;
        double w = Math.abs(r);
        return ((1 - w) * c) + (w * ((r + 1) / 2));
    }
}
