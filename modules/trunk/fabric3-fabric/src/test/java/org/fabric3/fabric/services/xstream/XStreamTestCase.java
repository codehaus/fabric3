package org.fabric3.fabric.services.xstream;

import com.thoughtworks.xstream.XStream;
import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class XStreamTestCase extends TestCase {

    public void testCreate() {
        XStreamFactory factory = new XStreamFactoryImpl();
        XStream xstream = factory.createInstance();
        String output = xstream.toXML("hello");
        assertEquals("<?xml version='1.0' encoding='UTF-8'?><string>hello</string>", output);
        assertEquals("hello", xstream.fromXML(output));
    }

}
