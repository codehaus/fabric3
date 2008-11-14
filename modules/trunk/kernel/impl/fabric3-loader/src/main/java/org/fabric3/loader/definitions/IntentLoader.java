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
package org.fabric3.loader.definitions;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidPrefixException;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;
import org.fabric3.loader.impl.InvalidQNamePrefix;
import org.fabric3.scdl.definitions.Intent;

/**
 * Loader for definitions.
 *
 * @version $Revision$ $Date$
 */
public class IntentLoader implements TypeLoader<Intent> {

    private final LoaderHelper helper;

    public IntentLoader(@Reference LoaderHelper helper) {
        this.helper = helper;
    }

    public Intent load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        validateAttributes(reader, context);
        String name = reader.getAttributeValue(null, "name");
        QName qName = new QName(context.getTargetNamespace(), name);

        String constrainsVal = reader.getAttributeValue(null, "constrains");
        QName constrains = null;
        if (constrainsVal != null) {
            try {
                constrains = helper.createQName(constrainsVal, reader);
            } catch (InvalidPrefixException e) {
                context.addError(new InvalidQNamePrefix(e.getPrefix(), reader));
                return null;
            }
        }

        String description = null;

        String requiresVal = reader.getAttributeValue(null, "requires");
        Set<QName> requires = new HashSet<QName>();
        if (requiresVal != null) {
            StringTokenizer tok = new StringTokenizer(requiresVal);
            while (tok.hasMoreElements()) {
                try {
                    QName id = helper.createQName(tok.nextToken(), reader);
                    requires.add(id);
                } catch (InvalidPrefixException e) {
                    context.addError(new InvalidQNamePrefix(e.getPrefix(), reader));
                    return null;
                }
            }
        }

        while (true) {
            switch (reader.next()) {
            case START_ELEMENT:
                if (DefinitionsLoader.DESCRIPTION.equals(reader.getName())) {
                    description = reader.getElementText();
                }
                break;
            case END_ELEMENT:
                if (DefinitionsLoader.INTENT.equals(reader.getName())) {
                    return new Intent(qName, description, constrains, requires);
                }
            }
        }

    }

    private void validateAttributes(XMLStreamReader reader, IntrospectionContext context) {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            if (!"name".equals(name) && !"constrains".equals(name) && !"requires".equals(name)) {
                context.addError(new UnrecognizedAttribute(name, reader));
            }
        }
    }

}
