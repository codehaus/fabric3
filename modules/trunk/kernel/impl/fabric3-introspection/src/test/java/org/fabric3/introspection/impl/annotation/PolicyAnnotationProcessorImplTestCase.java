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
package org.fabric3.introspection.impl.annotation;

import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.oasisopen.sca.annotation.Confidentiality;
import static org.oasisopen.sca.annotation.Confidentiality.CONFIDENTIALITY;
import static org.oasisopen.sca.annotation.Confidentiality.CONFIDENTIALITY_MESSAGE;
import org.oasisopen.sca.annotation.PolicySets;
import org.oasisopen.sca.annotation.Requires;

import org.fabric3.model.type.PolicyAware;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * @version $Revision$ $Date$
 */
public class PolicyAnnotationProcessorImplTestCase extends TestCase {
    private PolicyAnnotationProcessorImpl processor = new PolicyAnnotationProcessorImpl();

    public void testRequires() throws Exception {
        Requires annotation = TestClass.class.getAnnotation(Requires.class);
        IntrospectionContext ctx = new DefaultIntrospectionContext();

        QName qname = new QName("namespace", "foo");
        PolicyAware modelObject = EasyMock.createMock(PolicyAware.class);
        modelObject.addIntent(qname);
        EasyMock.expectLastCall();
        EasyMock.replay(modelObject);

        processor.process(annotation, modelObject, ctx);
        EasyMock.verify(modelObject);
    }

    public void testPolicySets() throws Exception {
        PolicySets annotation = TestPolicySet.class.getAnnotation(PolicySets.class);
        IntrospectionContext ctx = new DefaultIntrospectionContext();

        QName qname = new QName("namespace", "foo");
        PolicyAware modelObject = EasyMock.createMock(PolicyAware.class);
        modelObject.addPolicySet(qname);
        EasyMock.expectLastCall();
        EasyMock.replay(modelObject);

        processor.process(annotation, modelObject, ctx);
        EasyMock.verify(modelObject);
    }

    public void testUnQualified() throws Exception {
        Confidentiality annotation = TestClass.class.getAnnotation(Confidentiality.class);
        IntrospectionContext ctx = new DefaultIntrospectionContext();

        QName qname = QName.valueOf(CONFIDENTIALITY);
        PolicyAware modelObject = EasyMock.createMock(PolicyAware.class);
        modelObject.addIntent(qname);
        EasyMock.expectLastCall();
        EasyMock.replay(modelObject);

        processor.process(annotation, modelObject, ctx);
        EasyMock.verify(modelObject);
    }

    public void testQualified() throws Exception {
        Confidentiality annotation = TestQualifiedClass.class.getAnnotation(Confidentiality.class);
        IntrospectionContext ctx = new DefaultIntrospectionContext();

        QName qname = QName.valueOf(CONFIDENTIALITY_MESSAGE);
        PolicyAware modelObject = EasyMock.createMock(PolicyAware.class);
        modelObject.addIntent(qname);
        EasyMock.expectLastCall();
        EasyMock.replay(modelObject);

        processor.process(annotation, modelObject, ctx);
        EasyMock.verify(modelObject);
    }

    @Confidentiality
    @Requires("{namespace}foo")
    private class TestClass {


    }


    @Confidentiality(CONFIDENTIALITY_MESSAGE)
    private class TestQualifiedClass {

    }

    @PolicySets("{namespace}foo")
    private class TestPolicySet {

    }
}