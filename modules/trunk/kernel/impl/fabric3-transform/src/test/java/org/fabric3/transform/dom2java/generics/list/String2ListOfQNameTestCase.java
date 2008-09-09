package org.fabric3.transform.dom2java.generics.list;

import java.io.StringReader;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * @version $Revision$ $Date$
 */
public class String2ListOfQNameTestCase extends TestCase {
    private static final DocumentBuilderFactory DOCUMENT_FACTORY;
    private static final String PREFIX = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    private static final String NO_NAMESPACES_XML = PREFIX + "<test>zero, one, two</test>";
    private static final String NAMESPACES_XML = PREFIX + "<test>{ns}zero, {ns}one, {ns}two</test>";

    static {
        DOCUMENT_FACTORY = DocumentBuilderFactory.newInstance();
        DOCUMENT_FACTORY.setNamespaceAware(true);
    }

    private String2ListOfQName transformer;

    public void testNoNamespacesTransform() throws Exception {
        Element element = createTestNode(NO_NAMESPACES_XML);
        List<QName> list = transformer.transform(element, null);
        assertEquals(3, list.size());
        assertEquals("zero", list.get(0).getLocalPart());
        assertEquals("one", list.get(1).getLocalPart());
        assertEquals("two", list.get(2).getLocalPart());
    }

    public void testNamespacesTransform() throws Exception {
        Element element = createTestNode(NAMESPACES_XML);
        List<QName> list = transformer.transform(element, null);
        assertEquals(3, list.size());
        assertEquals("ns", list.get(0).getNamespaceURI());
        assertEquals("ns", list.get(1).getNamespaceURI());
        assertEquals("ns", list.get(2).getNamespaceURI());
    }

    private Element createTestNode(String xml) throws Exception {
        InputSource source = new InputSource(new StringReader(xml));
        Document document = DOCUMENT_FACTORY.newDocumentBuilder().parse(source);
        return document.getDocumentElement();
    }

    protected void setUp() throws Exception {
        super.setUp();
        transformer = new String2ListOfQName();

    }
}
