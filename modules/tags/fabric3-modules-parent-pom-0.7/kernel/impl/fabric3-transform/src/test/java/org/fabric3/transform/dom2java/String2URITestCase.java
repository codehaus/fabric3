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

import java.net.URI;
import java.net.URISyntaxException;

import org.fabric3.spi.transform.TransformationException;


/**
 * Tests String to URI Transform
 */
public class  String2URITestCase extends BaseTransformTest {

	/**
	 * Test for successful transformation from String to URI 
	 */
	public void testURITransformSuccess() {
		final String uriContent = "xmlns:f3";
		final String xml = "<string_to_uri>" + uriContent + "</string_to_uri>";
		
		try {
			final URI transformedURI = getStringToURI().transform(getNode(xml), null);
			assertNotNull(transformedURI);
			assertEquals(uriContent, transformedURI.toString());
		} catch (TransformationException te) {
			fail("TransformationException : - Should Not Occur" + te);
		} catch (Exception e) {
			fail("Unexpexcted Exception Should not occur " + e);
		}
	}
	
	/**
	 * Test for unsuccessful Conversion from String URI
	 */
	public void testURITransformationSuccess() {
		final String errorURIContent = "[[[[]]io9876^^^hasx";
		final String xml = "<string_to_urierror>" + errorURIContent + "</string_to_urierror>";
		
		try {
			getStringToURI().transform(getNode(xml), null);
			fail("Should not convert to URI");
		} catch (TransformationException te) {
			assertNotNull(te);
		    URISyntaxException.class.isAssignableFrom(te.getClass());
		} catch (Exception e) {
			fail("Unexpexcted Exception Should not occur " + e);
		}
	}
	
	/**
	 * @return StringToURI
	 */
	private String2URI getStringToURI() {
		return new String2URI();
	}
}
