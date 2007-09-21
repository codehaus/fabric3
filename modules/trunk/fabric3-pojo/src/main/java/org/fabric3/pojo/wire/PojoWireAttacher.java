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
package org.fabric3.pojo.wire;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.fabric3.pojo.implementation.PojoComponent;
import org.fabric3.spi.builder.component.WireAttacher;
import org.fabric3.spi.model.instance.ValueSource;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.model.type.JavaClass;
import org.fabric3.spi.model.type.XSDSimpleType;
import org.fabric3.spi.transform.PullTransformer;
import org.fabric3.spi.transform.TransformContext;
import org.fabric3.spi.transform.TransformerRegistry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @version $Revision$ $Date$
 */
public abstract class PojoWireAttacher<PWSD extends PhysicalWireSourceDefinition, PWTD extends PhysicalWireTargetDefinition> implements WireAttacher<PWSD, PWTD> {

    private static final XSDSimpleType SOURCE_TYPE = new XSDSimpleType(Node.class, XSDSimpleType.STRING);

    private TransformerRegistry<PullTransformer<?, ?>> transformerRegistry;
    
    protected PojoWireAttacher(TransformerRegistry<PullTransformer<?, ?>> transformerRegistry) {
        this.transformerRegistry = transformerRegistry;
    }

    protected Object getKey(PhysicalWireSourceDefinition sourceDefinition, PojoComponent<?> source, ValueSource referenceSource) {
        
        Document keyDocument = sourceDefinition.getKey();
        
        if(keyDocument != null) {
            

            Element element = keyDocument.getDocumentElement();
            
            Class<?> formalType = null;
            Type type = source.getGerenricMemberType(referenceSource);
            
            if(type instanceof ParameterizedType) { 
                ParameterizedType genericType = (ParameterizedType) type;
                formalType = (Class<?>) genericType.getActualTypeArguments()[0];
            } else {
                formalType = String.class;
            }
            // TODO Pass the parameters in
            TransformContext context = new TransformContext(null, null, null, null);
            try {
                return createKey(formalType, element, context);
                // return keyDocument.getDocumentElement().getTextContent();
            } catch (Exception e) {
                throw new AssertionError(e);
            }
        }
        return null;
    }

    private <T> T createKey(Class<T> type, Element value, TransformContext context) {
        JavaClass<T> targetType = new JavaClass<T>(type);
        PullTransformer<Node, T> transformer = getTransformer(SOURCE_TYPE, targetType);
        try {
            return type.cast(transformer.transform(value, context));
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> PullTransformer<Node,T> getTransformer(XSDSimpleType source, JavaClass<T> target) {
        return (PullTransformer<Node,T>) transformerRegistry.getTransformer(source, target);
    }

}
