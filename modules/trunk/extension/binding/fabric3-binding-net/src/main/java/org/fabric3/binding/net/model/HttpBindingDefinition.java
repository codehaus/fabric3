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
package org.fabric3.binding.net.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;

import org.oasisopen.sca.Constants;
import org.w3c.dom.Document;

import org.fabric3.model.type.component.BindingDefinition;
import org.fabric3.model.type.component.Encodings;

/**
 * Represents a binding.http configuration.
 *
 * @version $Revision$ $Date$
 */
public class HttpBindingDefinition extends BindingDefinition {
    private static final long serialVersionUID = 1035192281713003125L;
    private String readTimeout;
    private int numberOfRetries;
    private List<String> methods = new ArrayList<String>();
    private String wireFormat;
    private String responseWireFormat;
    private String sslSettings;
    private String authenticationType;
    private String authenticationCredentials;
    private Headers headers;
    private List<ProxySetting> proxySettings = new ArrayList<ProxySetting>();
    private List<OperationProperty> operationProperties = new ArrayList<OperationProperty>();

    public HttpBindingDefinition(URI targetUri, Document key) {
        super(targetUri, new QName(Constants.SCA_NS, "binding.http"), key);
    }

    public String getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(String readTimeout) {
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

    public Headers getHeaders() {
        return headers;
    }

    public void setHeaders(Headers headers) {
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

    @Override
    public String getEncoding() {
        return Encodings.ASCII;
    }
}
