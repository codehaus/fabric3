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
package org.fabric3.loader.composite;

import javax.xml.namespace.QName;

import org.fabric3.spi.loader.LoaderException;

/**
 * Base class for exceptions raised during the load of a composite XML file.
 *
 * @version $Rev$ $Date$
 */
public abstract class CompositeLoaderException extends LoaderException {
    private QName compositeName;

    public CompositeLoaderException() {
        super();
    }

    public CompositeLoaderException(Throwable cause) {
        super(cause);
    }

    public QName getCompositeName() {
        return compositeName;
    }

    public void setCompositeName(QName compositeName) {
        this.compositeName = compositeName;
    }

    public String toString() {
        String resourceURI = getResourceURI();
        int line = getLine();
        int column = getColumn();

        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getName());
        builder.append(" in ");
        if (compositeName != null) {
            builder.append("composite ").append(compositeName).append(" file ");
        }
        builder.append(resourceURI == null ? "unknown" : resourceURI);
        if (line != -1) {
            builder.append(" at ").append(line).append(',').append(column);
        }
        builder.append(": ");
        builder.append(getLocalizedMessage());
        return builder.toString();
    }
}
