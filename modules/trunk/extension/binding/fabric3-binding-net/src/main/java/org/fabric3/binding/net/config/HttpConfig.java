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

import java.util.ArrayList;
import java.util.List;

import org.fabric3.binding.net.model.ProxySetting;

/**
 * Configuration for HTTP communications.
 *
 * @version $Revision$ $Date$
 */
public class HttpConfig extends BaseConfig {
    private static final long serialVersionUID = 8322571803453217170L;
    private String authenticationType;
    private String authenticationCredentials;
    private HttpHeaders headers;
    private List<ProxySetting> proxySettings = new ArrayList<ProxySetting>();
    private List<OperationProperty> operationProperties = new ArrayList<OperationProperty>();


    public String getAuthenticationType() {
        return authenticationType;
    }

    public void setAuthenticationType(String authenticationType) {
        this.authenticationType = authenticationType;
    }

    public String getAuthenticationCredentials() {
        return authenticationCredentials;
    }

    public void setAuthenticationCredentials(String authenticationCredentials) {
        this.authenticationCredentials = authenticationCredentials;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public void setHeaders(HttpHeaders headers) {
        this.headers = headers;
    }

    public List<ProxySetting> getProxySettings() {
        return proxySettings;
    }

    public void setProxySettings(List<ProxySetting> proxySettings) {
        this.proxySettings = proxySettings;
    }

    public List<OperationProperty> getOperationProperties() {
        return operationProperties;
    }

    public void setOperationProperties(List<OperationProperty> operationProperties) {
        this.operationProperties = operationProperties;
    }

}