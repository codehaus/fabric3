package org.fabric3.tests.function.headers;

import org.osoa.sca.annotations.OneWay;

/**
 * @version $Revision$ $Date$
 */
public interface AsyncHeaderService {

    @OneWay
    void invokeTestHeader(HeaderFuture future);

    @OneWay
    public void invokeTestHeaderCleared(HeaderFuture future);

}