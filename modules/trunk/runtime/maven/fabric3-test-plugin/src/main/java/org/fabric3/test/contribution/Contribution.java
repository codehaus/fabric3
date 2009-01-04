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
 */
package org.fabric3.test.contribution;

import java.net.URL;

/**
 * Represents a contribution identified from the scan.
 *
 */
public class Contribution {
    
    private URL location;
    private boolean extension;
    private boolean test;

    /**
     * Iniitiates the state of the contribution.
     * 
     * @param extension Whether the contribution is an extension.
     * @param location Location of the contribution.
     * @param test Whether the contribution is a test.
     */
    public Contribution(boolean extension, URL location, boolean test) {
        this.extension = extension;
        this.location = location;
        this.test = test;
    }
    
    /**
     * Gets the location of the contribution.
     * 
     * @return Location of the contribution.
     */
    public URL getLocation() {
        return location;
    }

    /**
     * Gets whether the identified contribution is an extension.
     * 
     * @return True if the contribution is an extension.
     */
    public boolean isExtension() {
        return extension;
    }
    
    /**
     * Gets whether the identified contribution is a test. This is 
     * the contents of the target/test-classes directory of the project.
     * 
     * @return True if this is a test contribution.
     */
    public boolean isTest() {
        return test;
    }

}
