package org.fabric3.host.runtime;

import java.net.URL;

/**
 * @version $Rev$ $Date$
 */
public interface ScdlBootstrapper extends Bootstrapper {
    /**
     * Returns the location of the SCDL used to boot this runtime.
     *
     * @return the location of the SCDL used to boot this runtime
     */
    URL getScdlLocation();

    /**
     * Sets the location of the SCDL used to boot this runtime.
     *
     * @param scdlLocation the location of the SCDL used to boot this runtime
     */
    void setScdlLocation(URL scdlLocation);
}
