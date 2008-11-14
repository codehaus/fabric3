/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.fabric.services.contribution.manifest;

import java.net.URI;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;

import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;
import org.fabric3.spi.services.contribution.QNameImport;

/**
 * Processes a QName-based <code>import</code> element in a contribution manifest
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class QNameImportLoader implements TypeLoader<QNameImport> {

    public QNameImport load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        validateAttributes(reader, context);
        String ns = reader.getAttributeValue(null, "namespace");
        if (ns == null) {
            MissingMainifestAttribute failure = new MissingMainifestAttribute("The namespace attribute must be specified", "namespace", reader);
            context.addError(failure);
            return null;
        }
        String location = reader.getAttributeValue(null, "location");
        QNameImport contributionImport = new QNameImport(new QName(ns));
        if (location != null) {
            contributionImport.setLocation(URI.create(location));
        }
        return contributionImport;
    }

        private void validateAttributes(XMLStreamReader reader, IntrospectionContext context) {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            if (!"namespace".equals(name)) {
                context.addError(new UnrecognizedAttribute(name, reader));
            }
        }
    }
}
