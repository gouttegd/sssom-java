/*
 * SSSOM-Java - SSSOM library for Java
 * Copyright © 2026 Damien Goutte-Gattat
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConfidenceHelperTest {

    private final static double EPSILON = 1e-9;
    private ConfidenceHelper helper = new ConfidenceHelper();

    @Test
    void testAggregateConfidence() {
        Mapping m = new Mapping();
        Assertions.assertTrue(compare(1.0, helper.aggregate(m)));

        m.setConfidence(0.8);
        m.setReviewerAgreement(-.2);
        Assertions.assertTrue(compare(0.72, helper.aggregate(m)));

        m.setReviewerAgreement(0.0);
        Assertions.assertTrue(compare(0.8, helper.aggregate(m)));
    }

    private boolean compare(double d1, double d2) {
        return Math.abs(d1 - d2) < EPSILON;
    }
}
