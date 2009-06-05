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
package org.fabric3.binding.net.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Common configuration for socket communications.
 *
 * @version $Revision$ $Date$
 */
public abstract class BaseConfig implements Serializable {
    private static final long serialVersionUID = -7562937002327535329L;
    private long readTimeout = -1;
    private int numberOfRetries = -1;
    private List<String> methods = new ArrayList<String>();
    private String wireFormat;
    private String responseWireFormat;
    private String sslSettings;


    public long getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(long readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getNumberOfRetries() {
        return numberOfRetries;
    }

    public void setNumberOfRetries(int numberOfRetries) {
        this.numberOfRetries = numberOfRetries;
    }

    public List<String> getMethods() {
        return methods;
    }

    public void setMethods(List<String> methods) {
        this.methods = methods;
    }

    public String getWireFormat() {
        return wireFormat;
    }

    public void setWireFormat(String wireFormat) {
        this.wireFormat = wireFormat;
    }

    public String getResponseWireFormat() {
        return responseWireFormat;
    }

    public void setResponseWireFormat(String responseWireFormat) {
        this.responseWireFormat = responseWireFormat;
    }

    public String getSslSettings() {
        return sslSettings;
    }

    public void setSslSettings(String sslSettings) {
        this.sslSettings = sslSettings;
    }


}