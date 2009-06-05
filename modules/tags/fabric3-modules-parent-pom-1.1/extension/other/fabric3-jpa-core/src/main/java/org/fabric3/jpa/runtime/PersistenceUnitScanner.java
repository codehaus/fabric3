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
package org.fabric3.jpa.runtime;

import javax.persistence.spi.PersistenceUnitInfo;


/**
 *
 * @version $Revision$ $Date$
 */
public interface PersistenceUnitScanner {

    /**
     * Scans the lassloader for the specified persistence unit and creates 
     * an immutable representation of the information present in the matching 
     * persistence.xml file.
     * 
     * @param unitName Persistence unit name.
     * @param classLoader Classloader to scan.
     * @return Persistence unit information.
     */
    public abstract PersistenceUnitInfo getPersistenceUnitInfo(String unitName, ClassLoader classLoader);

}