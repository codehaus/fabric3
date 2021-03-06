/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.introspection.xml.definitions;

import java.net.URI;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.model.type.definitions.BindingType;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidPrefixException;
import org.fabric3.spi.introspection.xml.InvalidQNamePrefix;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;

/**
 * Loader for definitions.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class BindingTypeLoader implements TypeLoader<BindingType> {

    private final LoaderHelper helper;

    public BindingTypeLoader(@Reference LoaderHelper helper) {
        this.helper = helper;
    }

    public BindingType load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        validateAttributes(reader, context);

        String name = reader.getAttributeValue(null, "name");
        QName qName = LoaderUtil.getQName(name, context.getTargetNamespace(), reader.getNamespaceContext());
        
        Set<QName> alwaysProvides;
        try {
            alwaysProvides = helper.parseListOfQNames(reader, "alwaysProvides");
            Set<QName> mayProvide = helper.parseListOfQNames(reader, "mayProvide");
            LoaderUtil.skipToEndElement(reader);
            return new BindingType(qName, alwaysProvides, mayProvide);
        } catch (InvalidPrefixException e) {
            String prefix = e.getPrefix();
            URI uri = context.getContributionUri();
            context.addError(new InvalidQNamePrefix("The prefix " + prefix + " specified in the definitions.xml file in contribution " + uri
                    + " is invalid", reader));
        }
        return null;


    }

    private void validateAttributes(XMLStreamReader reader, IntrospectionContext context) {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            if (!"name".equals(name) && !"alwaysProvides".equals(name) && !"mayProvide".equals(name)) {
                context.addError(new UnrecognizedAttribute(name, reader));
            }
        }
    }


}
