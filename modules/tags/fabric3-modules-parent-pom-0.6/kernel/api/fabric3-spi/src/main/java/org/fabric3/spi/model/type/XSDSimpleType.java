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
import javax.xml.namespace.QName;

/**
 * Specialization of DataType for simple types from the XML Schema type system.
 *
 * @version $Rev$ $Date$
 */
public class XSDSimpleType extends XSDType {
    private static final long serialVersionUID = 1482637673051984949L;
    public static final QName STRING = new QName(XSD_NS, "string");

    public XSDSimpleType(Type physical, QName logical) {
        super(physical, logical);
    }
}
