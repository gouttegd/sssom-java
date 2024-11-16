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

package org.incenp.obofoundry.sssom.cli;

import java.util.ArrayList;
import java.util.HashMap;

import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.transform.IMappingTransformer;
import org.incenp.obofoundry.sssom.transform.SSSOMTransformApplication;
import org.incenp.obofoundry.sssom.transform.SSSOMTransformError;
import org.incenp.obofoundry.sssom.transform.SSSOMTransformReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Basic tests for the SSSOM/T-Mapping language.
 */
public class SSSOMTMappingTest {

    SSSOMTransformApplication<Mapping> application;

    SSSOMTMappingTest() {
        application = new SSSOMTransformApplication<Mapping>();
        application.registerGenerator(new SSSOMTIncludeFunction());
    }

    @Test
    void testRecogniseSSSOMTMappingFunction() {
        SSSOMTransformReader<Mapping> reader = new SSSOMTransformReader<Mapping>(application);

        Assertions.assertTrue(reader.read("predicate==* -> include()"));
    }

    @Test
    void testParsingErrorOnUnknownFunction() {
        SSSOMTransformReader<Mapping> reader = new SSSOMTransformReader<Mapping>(application);

        Assertions.assertFalse(reader.read("predicate==* -> foo()"));
    }

    @Test
    void testIncludeFunction() {
        try {
            IMappingTransformer<Mapping> o = application.onGeneratingAction("include", new ArrayList<String>(),
                    new HashMap<String, String>());
            Mapping m = new Mapping();
            Mapping included = o.transform(m);
            Assertions.assertEquals(m, included);
        } catch ( SSSOMTransformError e ) {
            Assertions.fail(e);
        }
    }
}
