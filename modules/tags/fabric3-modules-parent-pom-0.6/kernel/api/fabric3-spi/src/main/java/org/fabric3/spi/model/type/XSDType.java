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
package org.fabric3.spi.model.type;

import java.lang.reflect.Type;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.fabric3.scdl.DataType;

/**
 * Specialization of DataType representing types from the XML Schema type system.
 *
 * @version $Rev$ $Date$
 */
public abstract class XSDType extends DataType<QName> {
    private static final long serialVersionUID = 4837060732513291971L;
    public static final String XSD_NS = XMLConstants.W3C_XML_SCHEMA_NS_URI;

    protected XSDType(Type physical, QName logical) {
        super(physical, logical);
    }
}
