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

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.TypeLoader;

/**
 * Loads implementation.mock from the scdl. The XML fragment is expeced to look like,
 * <p/>
 * <implementation.mock> org.fabric3.mock.Foo org.fabric3.mock.Bar org.fabric3.mock.Baz </implementation.mock>
 * <p/>
 * The implementation.mock element is expected to have a delimitted list of fully qualified named of the interfaces that need to be mocked.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class ImplementationMockLoader implements TypeLoader<ImplementationMock> {

    private final MockComponentTypeLoader componentTypeLoader;

    /**
     * Initializes the loader registry.
     *
     * @param componentTypeLoader Component type loader.
     */
    @SuppressWarnings("deprecation")
    public ImplementationMockLoader(@Reference MockComponentTypeLoader componentTypeLoader) {
        this.componentTypeLoader = componentTypeLoader;
    }

    /**
     * Loads implementation.mock element from the SCDL.
     *
     * @param reader  StAX reader using which the scdl is loaded.
     * @param context Loader context containing contextual information.
     * @return An instance of mock implementation.
     */
    public ImplementationMock load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {

        assert reader.getName().equals(ImplementationMock.IMPLEMENTATION_MOCK);

        String textualContent = reader.getElementText().trim();

        List<String> mockedInterfaces = new ArrayList<String>();

        StringTokenizer tok = new StringTokenizer(textualContent);
        while (tok.hasMoreElements()) {
            mockedInterfaces.add(tok.nextToken().trim());
        }

        MockComponentType componentType = componentTypeLoader.load(mockedInterfaces, context);

        assert reader.getName().equals(ImplementationMock.IMPLEMENTATION_MOCK);

        return new ImplementationMock(mockedInterfaces, componentType);

    }

}
