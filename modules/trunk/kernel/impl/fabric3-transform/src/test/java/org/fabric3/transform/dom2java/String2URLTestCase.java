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
package org.fabric3.transform.dom2java;

import java.net.MalformedURLException;
import java.net.URL;

import org.fabric3.spi.transform.TransformationException;


/**
 * Tests String to URL Transformation
 */
public class  String2URLTestCase extends BaseTransformTest {

	/**
	 * Test for successful transformation from String to URL 
	 */
	public void testURLTransformSuccess() {
		final String urlContent = "ftp://testf3.org";
		final String xml = "<string_to_url>" + urlContent + "</string_to_url>";
		
		try {
			final URL transformedURL = getStringToURL().transform(getNode(xml), null);
			assertNotNull(transformedURL);
		} catch (TransformationException te) {
			fail("TransformationException : - Should Not Occur" + te);
		} catch (Exception e) {
			fail("Unexpexcted Exception Should not occur " + e);
		}
	}
	
	/**
	 * Test for unsuccessful Conversion from String URL
	 */
	public void testURLTransformationSuccess() {
		final String erroredURL = "failedURL";
		final String xml = "<string_to_urlerror>" + erroredURL + "</string_to_urlerror>";
		
		try {
			getStringToURL().transform(getNode(xml), null);
			fail("Should not convert to URL");
		} catch (TransformationException te) {
			assertNotNull(te);
			MalformedURLException.class.isAssignableFrom(te.getCause().getClass());
		} catch (Exception e) {
			fail("Unexpexcted Exception Should not occur " + e);
		}
	}
	
	/**
	 * @return StringToURL
	 */
	private String2URL getStringToURL() {
		return new String2URL();
	}
}
