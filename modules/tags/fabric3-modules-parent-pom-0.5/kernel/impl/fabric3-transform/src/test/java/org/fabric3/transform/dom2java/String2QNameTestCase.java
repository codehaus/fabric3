/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
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
