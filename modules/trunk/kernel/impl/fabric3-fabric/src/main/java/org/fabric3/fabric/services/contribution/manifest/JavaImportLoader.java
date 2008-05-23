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
package org.fabric3.fabric.services.contribution.manifest;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;

import org.fabric3.fabric.services.contribution.MissingPackage;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.TypeLoader;

/**
 * Processes a <code>import.java</code> element in a contribution manifest
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class JavaImportLoader implements TypeLoader<JavaImport> {

    public JavaImport load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        String packageName = reader.getAttributeValue(null, "package");
        if (packageName == null) {
            MissingPackage failure = new MissingPackage("No package name specified", reader);
            context.addError(failure);
            return null;
        }
        return new JavaImport(packageName);
    }
}
