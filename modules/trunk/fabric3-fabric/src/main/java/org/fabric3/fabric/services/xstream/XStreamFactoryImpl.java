package org.fabric3.fabric.services.xstream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * Default implemenation of XStreamFactory. The factory may be configured with custom converters and drivers.
 *
 * @version $Rev$ $Date$
 */
public class XStreamFactoryImpl implements XStreamFactory {

    public XStream createInstance() {
        return new XStream(new StaxDriver());
    }

}
