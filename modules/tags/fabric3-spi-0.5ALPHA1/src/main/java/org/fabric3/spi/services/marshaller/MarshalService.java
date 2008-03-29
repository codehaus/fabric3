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
package org.fabric3.spi.services.marshaller;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * Converts an object to and from an XML representation.
 *
 * @version $Rev$ $Date$
 */
public interface MarshalService {
    /**
     * Marshalls an object.
     *
     * @param object object to be marshalled.
     * @param writer Writer to which marshalled information is written.
     * @throws MarshalException if an error is encountered marshalling
     */
    void marshall(Object object, XMLStreamWriter writer) throws MarshalException;

    /**
     * Unmarshalls an XML stream to an object.
     *
     * @param type   the unmarshalled type
     * @param reader Reader from which marshalled information is read.
     * @return object from the marshalled stream.
     * @throws MarshalException if an error is encountered unmarshalling
     */
    <T> T unmarshall(Class<T> type, XMLStreamReader reader) throws MarshalException;

}
