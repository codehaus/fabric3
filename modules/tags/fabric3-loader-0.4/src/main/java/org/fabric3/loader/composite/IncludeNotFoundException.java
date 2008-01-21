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

/**
 * Exception that indicates that the composite named in an include could not be located.
 *
 * @version $Rev$ $Date$
 */
public class IncludeNotFoundException extends CompositeLoaderException {
    private final QName includedCompositeName;

    public IncludeNotFoundException(QName includedCompositeName) {
        this.includedCompositeName = includedCompositeName;
    }

    public QName getIncludedCompositeName() {
        return includedCompositeName;
    }

    public String getMessage() {
        return "Unable to include composite with name " + includedCompositeName + " (currently scdlResource or scdlLocation must be specified).";
    }
}