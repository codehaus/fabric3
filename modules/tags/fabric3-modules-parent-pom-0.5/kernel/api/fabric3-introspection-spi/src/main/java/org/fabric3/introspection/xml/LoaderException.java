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

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.Location;

import org.fabric3.introspection.IntrospectionException;

/**
 * Base class for Exceptions raised during the loading process. Loader implementations should throw a subclass of this to indicate the actual
 * problem.
 *
 * @version $Rev$ $Date$
 */
public class LoaderException extends IntrospectionException {
    public static final int UNDEFINED = -1;
    private static final long serialVersionUID = -7459051598906813461L;
    private final String resourceURI;
    private final int line;
    private final int column;

    public LoaderException(String message, XMLStreamReader reader) {
        super(message);
        Location location = reader.getLocation();
        line = location.getLineNumber();
        column = location.getColumnNumber();
        resourceURI = location.getSystemId();
    }

    public LoaderException(String message, Throwable cause) {
        super(message, cause);
        line = UNDEFINED;
        column = UNDEFINED;
        resourceURI = null;
    }

    /**
     * Returns the location of the resource that was being loaded.
     *
     * @return the location of the resource that was being loaded
     */
    public String getResourceURI() {
        return resourceURI;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getName());
        builder.append(" in ");
        builder.append(resourceURI == null ? "unknown" : resourceURI);
        if (line != -1) {
            builder.append(" at ").append(line).append(',').append(column);
        }
        builder.append(": ");
        builder.append(getLocalizedMessage());
        return builder.toString();
    }
}
