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

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.scdl.ValidationFailure;

/**
 * Base class for validation failures occuring in XML artifacts.
 *
 * @version $Revision$ $Date$
 */
public abstract class XmlValidationFailure<T> extends ValidationFailure<T> {
    private final int line;
    private final int column;
    private final String message;
    private String resourceURI;

    protected XmlValidationFailure(String message, T modelObject, XMLStreamReader reader) {
        super(modelObject);
        this.message = message;
        Location location = reader.getLocation();
        line = location.getLineNumber();
        column = location.getColumnNumber();
        resourceURI = location.getSystemId();
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public String getResourceURI() {
        return resourceURI;
    }

    public String getMessage() {
        StringBuilder builder = new StringBuilder();
        builder.append(message);
        builder.append(" in ");
        builder.append(resourceURI == null ? "unknown" : resourceURI);
        if (line != -1) {
            builder.append(" at ").append(line).append(',').append(column);
        }
        return builder.toString();
    }

}
