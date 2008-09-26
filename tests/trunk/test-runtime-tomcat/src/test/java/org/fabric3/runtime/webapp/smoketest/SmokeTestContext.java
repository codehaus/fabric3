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
package org.fabric3.runtime.webapp.smoketest;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class SmokeTestContext extends TestCase {
    private URL base;

    public void testContext() throws IOException {
        URL url = new URL(base, "smoketest?test=context");
        String result = getContent(url);
        assertEquals("component URI is fabric3://domain/smoketest", result);
    }

    private String getContent(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        Reader reader = new InputStreamReader(connection.getInputStream());
        StringBuilder result = new StringBuilder();
        int ch;
        while ((ch = reader.read()) != -1) {
            result.append((char)ch);
        }
        reader.close();
        assertEquals(200, connection.getResponseCode());
        return result.toString();
    }

    protected void setUp() throws Exception {
        super.setUp();
        base = new URL("http://localhost:8900/test-runtime-tomcat/");
    }
}
