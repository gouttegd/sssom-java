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

import java.io.File;
import java.net.URI;

import javax.xml.catalog.Catalog;
import javax.xml.catalog.CatalogException;
import javax.xml.catalog.CatalogFeatures;
import javax.xml.catalog.CatalogFeatures.Feature;
import javax.xml.catalog.CatalogManager;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;

/**
 * An implementation of the OWLOntologyIRIMapper interface based on a XML
 * catalog. This is merely a wrapper around the standard Java XML Catalog API.
 */
public class XMLCatalogIRIMapper implements OWLOntologyIRIMapper {

    private static final long serialVersionUID = 4675201788064034974L;

    private URI catalogURI;
    private Catalog catalog;
    private boolean needsReset;

    /**
     * Creates a new instance from the specified file.
     * 
     * @param catalogFile The XML catalog file.
     * @throws CatalogException If an error occurs when parsing the catalog.
     */
    public XMLCatalogIRIMapper(File catalogFile) throws CatalogException {
        catalogURI = catalogFile.toURI();
        parseCatalog();

        /*
         * A bug in Java 11, not fixed until Java 19, means that the `matchURI` method
         * will always return the same value after the first successful lookup. For Java
         * < 19, the only workaround is to re-parse the entire catalog after every
         * successful lookup. :(
         * 
         * See <https://bugs.openjdk.org/browse/JDK-8253569>.
         */
        needsReset = Integer.valueOf(System.getProperty("java.specification.version")) < 19;
    }

    @Override
    public IRI getDocumentIRI(IRI ontologyIRI) {
        String resolved = catalog.matchURI(ontologyIRI.toString());
        if ( resolved != null ) {
            if ( needsReset ) {
                parseCatalog();
            }
            return IRI.create(resolved);
        }
        return null;
    }

    private void parseCatalog() {
     // @formatter:off
        CatalogFeatures features = CatalogFeatures.builder()
                .with(Feature.PREFER, "system")
                .with(Feature.DEFER, "false")
                .with(Feature.RESOLVE, "continue")
                .build();
        // @formatter:on
        catalog = CatalogManager.catalog(features, catalogURI);
    }

}
