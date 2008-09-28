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

import javax.xml.namespace.QName;

import org.fabric3.transform.TransformationException;

/**
 * Tests String to QName Transform
 */
public class String2QNameTestCase extends BaseTransformTest {

	/**
	 * Test of converting String to QName
	 */
	public void testQNameTransform() {
		final String Q_NAME = "<string_to_qname>{http://f3.com/ns/fabric/test}f3</string_to_qname>";
		try {
			final QName qname= getStringToQName().transform(getNode(Q_NAME), null);
			assertNotNull(qname);
			assertEquals("{http://f3.com/ns/fabric/test}f3", qname.toString());
			assertEquals("http://f3.com/ns/fabric/test", qname.getNamespaceURI());
			assertEquals("f3", qname.getLocalPart());
		} catch (TransformationException te) {
			fail("Transform exception should not occur " + te);
		} catch (Exception e) {
			fail("Unexpexcted Exception Should not occur " + e);
		}
	}
	
	/**
	 * Test of converting String to QName
	 */
	public void testQNameTransformWithNamespace() {
		final String Q_NAME = "<string_to_qname xmlns:foo='http://f3.com/ns/fabric/test'>foo:f3</string_to_qname>";
		try {
			final QName qname= getStringToQName().transform(getNode(Q_NAME), null);
			assertNotNull(qname);
			assertEquals("{http://f3.com/ns/fabric/test}f3", qname.toString());
			assertEquals("http://f3.com/ns/fabric/test", qname.getNamespaceURI());
			assertEquals("f3", qname.getLocalPart());
			assertEquals("foo", qname.getPrefix());
		} catch (TransformationException te) {
			fail("Transform exception should not occur " + te);
		} catch (Exception e) {
			fail("Unexpexcted Exception Should not occur " + e);
		}
	}

	/**
	 * Test failure of converting String to QName
	 */
	public void testQNameTransformFailure() {
		final String Q_NAME = "<string_to_qname>{}</string_to_qname>";
		try {
			getStringToQName().transform(getNode(Q_NAME), null);
			fail("Should not reach here something wrong in [ String2QName ] code");
		} catch (TransformationException te) {
			assertNotNull(te);
			assertTrue(IllegalArgumentException.class.isAssignableFrom(te.getCause().getClass()));
		} catch (Exception e) {
			fail("Unexpexcted Exception Should not occur " + e);
		}
	}

	/**
	 * @return
	 */
	private String2QName getStringToQName() {
		return new String2QName();
	}
}
