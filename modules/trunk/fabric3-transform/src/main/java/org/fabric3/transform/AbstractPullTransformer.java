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
package org.fabric3.transform;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Node;

import org.fabric3.scdl.DataType;
import org.fabric3.spi.model.type.XSDSimpleType;
import org.fabric3.spi.transform.PullTransformer;
import org.fabric3.spi.transform.TransformerRegistry;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public abstract class AbstractPullTransformer<SOURCE, TARGET> implements PullTransformer<SOURCE, TARGET> {
	
	/** Default source to be used */
	private static final XSDSimpleType DEFAULT_SOURCE = new XSDSimpleType(Node.class, XSDSimpleType.STRING);
	
    /** Transform Registry to be used*/
    private TransformerRegistry<PullTransformer<SOURCE, TARGET>> registry;

    /**
     * Set Registry
     * @param registry
     */
    @Reference
    public void setRegistry(TransformerRegistry<PullTransformer<SOURCE, TARGET>> registry) {
        this.registry = registry;
    }

    /** Register Transformer*/
    @Init
    public void init() {
        System.err.println(this);
    }

    /** Unregister Registry*/
    @Destroy
    public void destroy() {
        registry.unregister(this);
    }
    
    /**
     * @see org.fabric3.spi.transform.Transformer#getSourceType()
     */
    public DataType<?> getSourceType() {
    	return DEFAULT_SOURCE;
    }
}
