/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
 */
package org.fabric3.introspection.xml.template;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.model.type.ModelObject;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.AbstractValidatingTypeLoader;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.TemplateRegistry;

/**
 * General class for loading template definitions such as <code>&lt;binding.template&gt;</code>. This class is general and is designed to be
 * configured to support specific element types. It works by resolving the requested template using the {@link TemplateRegistry}.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class TemplateLoader extends AbstractValidatingTypeLoader<ModelObject> {
    private TemplateRegistry registry;
    private Class<? extends ModelObject> expectedType;

    @SuppressWarnings({"unchecked"})
    public TemplateLoader(@Reference TemplateRegistry registry, @Property(name = "expectedType") String expectedType) {
        this.registry = registry;
        try {
            this.expectedType = (Class<? extends ModelObject>) getClass().getClassLoader().loadClass(expectedType);
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
        addAttributes("name");
    }

    public ModelObject load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        validateAttributes(reader, context);

        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            MissingAttribute error = new MissingAttribute("Attribute name must be specified", reader);
            context.addError(error);
            LoaderUtil.skipToEndElement(reader);
            return null;
        }
        ModelObject parsed = registry.resolve(expectedType, name);
        if (parsed == null) {
            TemplateNotFound error = new TemplateNotFound(name, reader);
            context.addError(error);
        }
        LoaderUtil.skipToEndElement(reader);
        return parsed;
    }

}
