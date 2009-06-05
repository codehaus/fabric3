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
package org.fabric3.contribution.manifest;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;

import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.TypeLoader;

/**
 * Processes a <code>extends</code> element in a contribution manifest
 *
 * @version $Rev: 6368 $ $Date: 2008-12-29 16:30:06 -0800 (Mon, 29 Dec 2008) $
 */
@EagerInit
public class ExtendsLoader implements TypeLoader<ExtendsDeclaration> {

    public ExtendsDeclaration load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            MissingPackage failure = new MissingPackage("No name specified for extends declaration", reader);
            context.addError(failure);
            return null;
        }

        return new ExtendsDeclaration(name);
    }

}