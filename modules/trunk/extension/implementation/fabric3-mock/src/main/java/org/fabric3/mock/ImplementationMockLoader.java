/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
package org.fabric3.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.loader.StAXElementLoader;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

/**
 * Loads implementation.mock from the scdl. The XML fragment is expeced to look like,
 * 
 * <implementation.mock>
 *         org.fabric3.mock.Foo
 *         org.fabric3.mock.Bar
 *         org.fabric3.mock.Baz
 * </implementation.mock>
 * 
 * The implementation.mock element is expected to have a delimitted list of fully qualified named of the interfaces
 * that need to be mocked.
 * 
 * @version $Revision$ $Date$
 */
@EagerInit
public class ImplementationMockLoader implements StAXElementLoader<ImplementationMock> {

    @SuppressWarnings("deprecation")
    private final LoaderRegistry registry;
    private final MockComponentTypeLoader componentTypeLoader;

    /**
     * Initializes the loader registry.
     * 
     * @param registry Loader registry that is injected.
     * @param componentTypeLoader Component type loader.
     */
    @SuppressWarnings("deprecation")
    public ImplementationMockLoader(@Reference LoaderRegistry registry, @Reference MockComponentTypeLoader componentTypeLoader) {
        this.registry = registry;
        this.componentTypeLoader = componentTypeLoader;
    }

    /**
     * Registers with loader registry.
     */
    @SuppressWarnings("deprecation")
    @Init
    public void init() {
        registry.registerLoader(ImplementationMock.IMPLEMENTATION_MOCK, this);
    }

    /**
     * Unregisters with the loader registry.
     */
    @SuppressWarnings("deprecation")
    @Destroy
    public void destroy() {
        registry.unregisterLoader(ImplementationMock.IMPLEMENTATION_MOCK);
    }

    /**
     * Loads implementation.mock element from the SCDL.
     * 
     * @param reader StAX reader using which the scdl is loaded.
     * @param context Loader context containing contextual information.
     * @return An instance of mock implementation.
     * @throws LoaderException If unable to load implementation.mock from the SCDL.
     */
    public ImplementationMock load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException,
            LoaderException {
        
        assert reader.getName().equals(ImplementationMock.IMPLEMENTATION_MOCK);
        
        String textualContent = reader.getElementText().trim();
        
        List<String> mockedInterfaces = new ArrayList<String>();
        
        StringTokenizer tok = new StringTokenizer(textualContent);
        while(tok.hasMoreElements()) {
            mockedInterfaces.add(tok.nextToken().trim());
        }
        
        MockComponentType componentType = componentTypeLoader.load(mockedInterfaces, context);
        
        assert reader.getName().equals(ImplementationMock.IMPLEMENTATION_MOCK);
        
        return new ImplementationMock(mockedInterfaces, componentType);
        
    }

}
