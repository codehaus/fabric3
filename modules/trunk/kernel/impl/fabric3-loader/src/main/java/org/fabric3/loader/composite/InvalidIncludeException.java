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
package org.fabric3.loader.composite;

import javax.xml.namespace.QName;

import org.fabric3.spi.loader.LoaderException;

/**
 * Exception that indicates that the composite named in an include was not valid.
 *
 * @version $Rev$ $Date$
 */
public class InvalidIncludeException extends CompositeLoaderException {
    private static final long serialVersionUID = 2784600570114810462L;
    private final QName compositeName;

    public InvalidIncludeException(QName compositeName, LoaderException cause) {
        super(cause);
        this.compositeName = compositeName;
    }

    public QName getCompositeName() {
        return compositeName;
    }

    public String getMessage() {
        return "Invalid composite with name " + compositeName;
    }
}
