/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.transform;

import org.fabric3.scdl.DataType;
import org.fabric3.spi.model.type.XSDSimpleType;
import org.fabric3.spi.transform.PullTransformer;
import org.fabric3.spi.transform.TransformerRegistry;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Node;

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
        registry.register(this);
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
    
    /**
     * Checks whether this transformer can transform the specified type.
     * 
     * @param target Target type.
     * @return True if this type can be transformed.
     */
    public boolean canTransform(DataType<?> targetType) {
    	return false;
    }
}
