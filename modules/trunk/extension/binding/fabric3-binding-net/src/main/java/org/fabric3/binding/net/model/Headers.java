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

import java.util.HashMap;
import java.util.Map;

/**
 * @version $Revision$ $Date$
 */
public class Headers {
    private float HttpVersion = 1.1f;
    private String HttpContentEncoding;
    private String HttpTransferEncoding;
    private String HttpMediaType;
    private String HttpCharset;
    private String HttpMethod;
    private Map<String, String> properties = new HashMap<String, String>();

    public float getHttpVersion() {
        return HttpVersion;
    }

    public void setHttpVersion(float httpVersion) {
        HttpVersion = httpVersion;
    }

    public String getHttpContentEncoding() {
        return HttpContentEncoding;
    }

    public void setHttpContentEncoding(String httpContentEncoding) {
        HttpContentEncoding = httpContentEncoding;
    }

    public String getHttpTransferEncoding() {
        return HttpTransferEncoding;
    }

    public void setHttpTransferEncoding(String httpTransferEncoding) {
        HttpTransferEncoding = httpTransferEncoding;
    }

    public String getHttpMediaType() {
        return HttpMediaType;
    }

    public void setHttpMediaType(String httpMediaType) {
        HttpMediaType = httpMediaType;
    }

    public String getHttpCharset() {
        return HttpCharset;
    }

    public void setHttpCharset(String httpCharset) {
        HttpCharset = httpCharset;
    }

    public String getHttpMethod() {
        return HttpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        HttpMethod = httpMethod;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
}
