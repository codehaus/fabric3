/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
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
package org.fabric3.introspection.xml;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

/**
 * Exception that indicates an element was encountered that could not be handled.
 *
 * @version $Rev$ $Date$
 */
public class UnrecognizedElementException extends LoaderException {
    private static final long serialVersionUID = 2549543622209829032L;
    private final QName element;

    /**
     * Constructor that indicates which resource could not be found. The supplied parameter is also returned as the message.
     *
     * @param reader the StAX reader positioned on the unrecognized element
     */
    public UnrecognizedElementException(XMLStreamReader reader) {
        super("Unrecognized element", reader);
        this.element = reader.getName();
    }

    public QName getElement() {
        return element;
    }

    public String getMessage() {
        StringBuffer b = new StringBuffer("The element ").append(getElement());
        return b.append(
                " was not recognized. If this is not a typo, check to ensure extensions are configured properly.").toString();


    }
}
