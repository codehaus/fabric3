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
package org.fabric3.spring.applicationContext;

import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.core.io.Resource;

/**
 * An <code>ApplicationContext</code> specialization that registers namespace
 * handlers for SCA elements - in particular the <service/>, <reference/> and
 * <property/> elements which are provided as SCA extensions to the Spring
 * application context schema
 * 
 * @version
 */
public class SCAApplicationContext extends AbstractXmlApplicationContext {
    private Resource appXml;

    public SCAApplicationContext(ApplicationContext parent, Resource appXml, ClassLoader classLoader) {
        super(parent);
        setClassLoader(classLoader);
        this.appXml = appXml;
    }

    @Override
    protected void initBeanDefinitionReader(XmlBeanDefinitionReader beanDefinitionReader) {
        ClassLoader cl = getClassLoader();
//        beanDefinitionReader.setNamespaceHandlerResolver(new SCANamespaceHandlerResolver(cl));
    }

    @Override
    protected Resource[] getConfigResources() {
        return new Resource[] {appXml};
    }
}
