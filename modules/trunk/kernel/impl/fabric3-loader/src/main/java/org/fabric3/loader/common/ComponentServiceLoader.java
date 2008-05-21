/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.fabric3.loader.common;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.osoa.sca.Constants.SCA_NS;
import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.Loader;
import org.fabric3.introspection.xml.LoaderException;
import org.fabric3.introspection.xml.LoaderHelper;
import org.fabric3.introspection.xml.MissingAttribute;
import org.fabric3.introspection.xml.TypeLoader;
import org.fabric3.introspection.xml.UnrecognizedTypeException;
import org.fabric3.introspection.xml.UnrecognizedElementException;
import org.fabric3.introspection.xml.UnrecognizedElement;
import org.fabric3.scdl.BindingDefinition;
import org.fabric3.scdl.ComponentService;
import org.fabric3.scdl.ModelObject;
import org.fabric3.scdl.OperationDefinition;
import org.fabric3.scdl.ServiceContract;

/**
 * Loads a service definition from an XML-based assembly file
 *
 * @version $Rev$ $Date$
 */
public class ComponentServiceLoader implements TypeLoader<ComponentService> {
    private static final QName CALLBACK = new QName(SCA_NS, "callback");
    private final Loader loader;
    private final LoaderHelper loaderHelper;

    public ComponentServiceLoader(@Reference Loader loader,
                                  @Reference LoaderHelper loaderHelper) {
        this.loader = loader;
        this.loaderHelper = loaderHelper;
    }

    public ComponentService load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException, LoaderException {

        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            MissingAttribute failure = new MissingAttribute("Missing name attribute", "name", reader);
            context.addError(failure);
            return null;
        }
        ComponentService def = new ComponentService(name, null);

        loaderHelper.loadPolicySetsAndIntents(def, reader);

        boolean callback = false;
        while (true) {
            int i = reader.next();
            switch (i) {
            case XMLStreamConstants.START_ELEMENT:
                callback = CALLBACK.equals(reader.getName());
                if (callback) {
                    reader.nextTag();
                }
                ModelObject type;
                try {
                    type = loader.load(reader, ModelObject.class, context);
                    // TODO when the loader registry is replaced this try..catch must be replaced with a check for a loader and an
                    // UnrecognizedElement added to the context if none is found
                } catch (UnrecognizedElementException e) {
                    context.addError(new UnrecognizedElement(reader));
                    continue;
                }
                if (type instanceof ServiceContract) {
                    def.setServiceContract((ServiceContract<?>) type);
                } else if (type instanceof BindingDefinition) {
                    if (callback) {
                        def.addCallbackBinding((BindingDefinition) type);
                    } else {
                        def.addBinding((BindingDefinition) type);
                    }
                } else if (type instanceof OperationDefinition) {
                    def.addOperation((OperationDefinition) type);
                } else {
                    throw new UnrecognizedTypeException(reader);
                }
                break;
            case XMLStreamConstants.END_ELEMENT:
                if (callback) {
                    callback = false;
                    break;
                }
                return def;
            }
        }
    }
}
