package org.fabric3.fabric.assembly.normalizer;

import org.fabric3.spi.model.instance.LogicalComponent;

/**
 * Merges binding and other metadata on promoted services and references down the to leaf component they are initially
 * defined on.
 *
 * @version $Rev$ $Date$
 */
public interface PromotionNormalizer {

    /**
     * Performs the normalization operation on services and references defined by the given leaf component. The
     * hierarchy of containing components will be walked to determine the set of promoted services and references.
     *
     * @param component the leaf component
     */
    void normalize(LogicalComponent<?> component);

}
