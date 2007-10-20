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

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;

import org.w3c.dom.Document;

/**
 * @version $Rev$ $Date$
 */
public interface PropertyHelper {
    /**
     * Load an XML property value from a Stax stream.
     *
     * The reader must be positioned at an element whose body contains a property value.
     * This will typically be an SCA &lt;property&gt; element (either in a &lt;composite&gt; or in a &lt;component&gt;).
     * The resulting document comprises a &lt;value&gt; element whose body content will be that of the original
     * &lt;property&gt; element.
     * 
     * @param reader a stream containing a property value
     * @return a standalone document containing the value
     * @throws XMLStreamException if there was a problem reading the stream
     */
    Document loadValue(XMLStreamReader reader) throws XMLStreamException;
}
