/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.introspection.xml.common;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.Reference;

import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.introspection.ImplementationNotFoundException;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.ResourceNotFound;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;

/**
 * Loads a Java interface definition from an XML-based assembly file
 *
 * @version $Rev$ $Date$
 */
public class JavaInterfaceLoader implements TypeLoader<ServiceContract> {

    private final JavaContractProcessor contractProcessor;
    private final IntrospectionHelper helper;

    public JavaInterfaceLoader(@Reference JavaContractProcessor contractProcessor,
                               @Reference IntrospectionHelper helper) {
        this.contractProcessor = contractProcessor;
        this.helper = helper;
    }

    public ServiceContract load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        validateAttributes(reader, context);
        String name = reader.getAttributeValue(null, "interface");
        if (name == null) {
            MissingAttribute failure = new MissingAttribute("An interface must be specified using the class attribute", reader);
            context.addError(failure);
            return null;
        }
        Class<?> interfaceClass;
        try {
            interfaceClass = helper.loadClass(name, context.getClassLoader());
        } catch (ImplementationNotFoundException e) {
            ResourceNotFound failure = new ResourceNotFound("Interface not found: " + name, reader);
            context.addError(failure);
            return null;
        }

        name = reader.getAttributeValue(null, "callbackInterface");
        Class<?> callbackClass;
        try {
            callbackClass = (name != null) ? helper.loadClass(name, context.getClassLoader()) : null;
        } catch (ImplementationNotFoundException e) {
            ResourceNotFound failure = new ResourceNotFound("Callback interface not found: " + name, reader);
            context.addError(failure);
            return null;
        }

        LoaderUtil.skipToEndElement(reader);

        TypeMapping mapping = context.getTypeMapping(interfaceClass);
        if (mapping == null) {
            mapping = new TypeMapping();
            context.addTypeMapping(interfaceClass, mapping);
        }
        helper.resolveTypeParameters(interfaceClass, mapping);
        ServiceContract serviceContract = contractProcessor.introspect(interfaceClass, context);
        if (callbackClass != null) {
            helper.resolveTypeParameters(callbackClass, mapping);
            ServiceContract callbackContract = contractProcessor.introspect(callbackClass, context);
            serviceContract.setCallbackContract(callbackContract);
        }
        return serviceContract;
    }

    private void validateAttributes(XMLStreamReader reader, IntrospectionContext context) {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            if (!"interface".equals(name) && !"callbackInterface".equals(name)) {
                context.addError(new UnrecognizedAttribute(name, reader));
            }
        }
    }

}
