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

import org.incenp.obofoundry.sssom.PrefixManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UriExpressionTest {

    private PrefixManager pfxMgr;

    UriExpressionTest() {
        pfxMgr = new PrefixManager();
        pfxMgr.add("ORGENT", "https://example.org/entities/");
        pfxMgr.add("COMENT", "https://example.com/entities/");
    }

    @Test
    void testSimpleParse() {
        UriExpression expr = UriExpression
                .parse("https://example.org/schema/0001/(field1:'ORGENT:0001',field2:'COMENT:0011')", pfxMgr);

        Assertions.assertNotNull(expr);
        Assertions.assertEquals("https://example.org/schema/0001", expr.getSchema());
        Assertions.assertEquals("https://example.org/entities/0001", expr.getComponent("field1"));
        Assertions.assertEquals("https://example.com/entities/0011", expr.getComponent("field2"));
    }

    @Test
    void testParseBase64EncodedExpression() {
        UriExpression expr = UriExpression.parse(
                "https://example.org/schema/0001/KGZpZWxkMTonT1JHRU5UOjAwMDEnLGZpZWxkMjonQ09NRU5UOjAwMTEnKQo=", pfxMgr);

        Assertions.assertNotNull(expr);
        Assertions.assertEquals("https://example.org/entities/0001", expr.getComponent("field1"));
        Assertions.assertEquals("https://example.com/entities/0011", expr.getComponent("field2"));
    }

    @Test
    void testParseInvalidBase64() {
        UriExpression expr = UriExpression.parse("https://example.org/schema/0001/_not_base_64", pfxMgr);
        Assertions.assertNull(expr);

        expr = UriExpression.parse("https://example.org/schema/0001/KGZpZWxkMTonT1JHKABOOMMjonQ09NRU5UOjAwMTEnKQo=",
                pfxMgr);
        Assertions.assertNull(expr);
    }

    @Test
    void testParseInvalidJsonUrl() {
        UriExpression expr = UriExpression.parse("https://example.org/schema/0001/", pfxMgr);
        Assertions.assertNull(expr);

        expr = UriExpression.parse("https://example.org/schema/0001/not_json_content", pfxMgr);
        Assertions.assertNull(expr);

        expr = UriExpression.parse("https://example.org/schema/0001/(:'ORGENT:0001')", pfxMgr);
        Assertions.assertNull(expr);

        expr = UriExpression.parse("https://example.org/schema/0001/(field1/:'ORGENT:0001')", pfxMgr);
        Assertions.assertNull(expr);

        expr = UriExpression.parse("https://example.org/schema/0001/(field1:'ORGENT:0001',)", pfxMgr);
        Assertions.assertNull(expr);
    }
}
