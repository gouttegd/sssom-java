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
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.incenp.obofoundry.sssom.uriexpr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.incenp.obofoundry.sssom.PrefixManager;
import org.incenp.obofoundry.sssom.model.Mapping;
import org.incenp.obofoundry.sssom.transform.IMappingFilter;
import org.incenp.obofoundry.sssom.transform.IMappingTransformer;
import org.incenp.obofoundry.sssom.transform.MappingFormatter;
import org.incenp.obofoundry.sssom.transform.SSSOMTransformApplication;
import org.incenp.obofoundry.sssom.transform.SSSOMTransformError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UriExpressionSSSOMTSupportTest {

    SSSOMTransformApplication<Void> application;
    List<String> arguments;
    Map<String, String> keyedArguments;

    UriExpressionSSSOMTSupportTest() {
        application = new SSSOMTransformApplication<Void>();
        PrefixManager pm = new PrefixManager();
        pm.add("ORGENT", "https://example.org/entities/");
        pm.add("COMENT", "https://example.com/entities/");
        application.onInit(pm);

        arguments = new ArrayList<String>();
        keyedArguments = new HashMap<String, String>();
    }

    @Test
    void testUriExpressionContainsFunction() {
        arguments.add("%{subject_id}");
        arguments.add("field1");
        arguments.add("https://example.org/entities/0001");
        arguments.add("field2");
        arguments.add("https://example.com/entities/*");

        Mapping m = new Mapping();

        try {
            IMappingFilter filter = application.onFilter("uriexpr_contains", arguments, keyedArguments);
            Assertions.assertNotNull(filter);

            m.setSubjectId("https://example.org/schema/0001/(field1:'ORGENT:0001',field2:'COMENT:0011')");
            Assertions.assertTrue(filter.filter(m));

            m.setSubjectId("https://example.org/schema/0001/(field1:'ORGENT:0001',field3:'COMENT:0011')");
            Assertions.assertFalse(filter.filter(m));

            m.setSubjectId("https://example.org/schema/0001/(field1:'ORGENT:0002',field2:'COMENT:0011')");
            Assertions.assertFalse(filter.filter(m));

            m.setSubjectId("https://example.org/schema/0001/no_json_url");
            Assertions.assertFalse(filter.filter(m));
        } catch ( SSSOMTransformError e ) {
            Assertions.fail(e);
        }
    }

    @Test
    void testUriExpressionSlotValueFunction() {
        MappingFormatter formatter = application.getFormatter();

        String uri = "https://example.org/schema/0001/(field1:'ORGENT:0001',field2:'COMENT:0011')";

        Mapping m = new Mapping();
        m.setSubjectId(uri);

        IMappingTransformer<String> f = formatter.getTransformer("%{subject_id|uriexpr_slot_value(field1)}");
        Assertions.assertEquals("https://example.org/entities/0001", f.transform(m));

        f = formatter.getTransformer("%{subject_id|uriexpr_slot_value(field1)|short}");
        Assertions.assertEquals("ORGENT:0001", f.transform(m));

        f = formatter.getTransformer("%{subject_id|uriexpr_slot_value(field3)}");
        Assertions.assertEquals(uri, f.transform(m));
    }
}
