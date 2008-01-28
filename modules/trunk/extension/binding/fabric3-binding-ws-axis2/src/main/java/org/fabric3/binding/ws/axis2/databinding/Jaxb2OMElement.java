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
package org.fabric3.binding.ws.axis2.databinding;

import org.apache.axiom.om.OMElement;
import org.fabric3.scdl.DataType;
import org.fabric3.spi.model.type.JavaClass;
import org.fabric3.spi.transform.TransformContext;
import org.fabric3.transform.AbstractPullTransformer;

/**
 * @version $Revision$ $Date$
 */
public class Jaxb2OMElement extends AbstractPullTransformer<Object, OMElement> {
    
    private static final JavaClass<OMElement> TARGET = new JavaClass<OMElement>(OMElement.class);
    
    private String packageName;
    
    public Jaxb2OMElement(String packageName) {
        this.packageName = packageName;
    }

    public OMElement transform(Object source, TransformContext context) {
        
        // TODO use the marshaller
        return null;
    }

    public DataType<?> getTargetType() {
        return TARGET;
    }

}
