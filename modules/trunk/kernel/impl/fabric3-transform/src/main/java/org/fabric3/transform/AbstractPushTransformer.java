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

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.model.type.service.DataType;
import org.fabric3.spi.transform.PushTransformer;
import org.fabric3.spi.transform.TransformerRegistry;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public abstract class AbstractPushTransformer<SOURCE, TARGET> implements PushTransformer<SOURCE, TARGET> {
    private TransformerRegistry<PushTransformer<SOURCE, TARGET>> registry;

    @Reference
    public void setRegistry(TransformerRegistry<PushTransformer<SOURCE, TARGET>> registry) {
        this.registry = registry;
    }

    @Init
    public void init() {
        registry.register(this);
    }

    @Destroy
    public void destroy() {
        registry.unregister(this);
    }

    /**
     * Checks whether this transformer can transform the specified type.
     *
     * @param targetType Target type.
     * @return True if this type can be transformed.
     */
    public boolean canTransform(DataType<?> targetType) {
        return false;
    }
}
