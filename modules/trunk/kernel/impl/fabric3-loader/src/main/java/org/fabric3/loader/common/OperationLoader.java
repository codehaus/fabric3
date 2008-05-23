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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.LoaderHelper;
import org.fabric3.introspection.xml.LoaderUtil;
import org.fabric3.introspection.xml.MissingAttribute;
import org.fabric3.introspection.xml.TypeLoader;
import org.fabric3.scdl.OperationDefinition;

/**
 * Loads an operation definition from the SCDL.
 *
 * @version $Rev: 1980 $ $Date: 2007-11-13 17:31:55 +0000 (Tue, 13 Nov 2007) $
 */
@EagerInit
public class OperationLoader implements TypeLoader<OperationDefinition> {

    private final LoaderHelper loaderHelper;

    public OperationLoader(@Reference LoaderHelper loaderHelper) {
        this.loaderHelper = loaderHelper;
    }

    public OperationDefinition load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {

        OperationDefinition operationDefinition = new OperationDefinition();
        String name = reader.getAttributeValue(null, "name");
        if (name == null) {
            MissingAttribute failure = new MissingAttribute("Operation name not specified", "name", reader);
            context.addError(failure);
        }
        operationDefinition.setName(name);

        loaderHelper.loadPolicySetsAndIntents(operationDefinition, reader, context);

        LoaderUtil.skipToEndElement(reader);

        return operationDefinition;

    }

}
