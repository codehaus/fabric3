package org.fabric3.fabric.services.xstream;

import com.thoughtworks.xstream.XStream;

/**
 * Default implemenation of XStreamFactory. The factory may be configured with custom converters and drivers.
 *
 * @version $Rev$ $Date$
 */
public class XStreamFactoryImpl implements XStreamFactory {

    public XStream createInstance() {
        return new XStream(new ClassLoaderStaxDriver(XStreamFactoryImpl.class.getClassLoader()));
    }

}
