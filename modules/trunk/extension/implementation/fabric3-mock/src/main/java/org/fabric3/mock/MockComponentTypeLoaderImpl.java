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

import java.util.List;

import org.fabric3.pojo.scdl.JavaMappedService;
import org.fabric3.spi.idl.java.JavaServiceContract;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;

/**
 * @version $Revision$ $Date$
 */
public class MockComponentTypeLoaderImpl implements MockComponentTypeLoader {
    
    /**
     * Loads the mock component type.
     * 
     * @param interfaces Interfaces that need to be mocked.
     * @param loaderContext Loader context.
     * @return Mock component type.
     */
    public MockComponentType load(List<String> mockedInterfaces, LoaderContext loaderContext) throws LoaderException {
        
        try {
            
            MockComponentType componentType = new MockComponentType();
            
            ClassLoader classLoader = loaderContext.getTargetClassLoader();
            int count = 0;
            for(String mockedInterface : mockedInterfaces) {
                Class<?> interfaceClass = classLoader.loadClass(mockedInterface);
                JavaServiceContract serviceContract = new JavaServiceContract(interfaceClass);
                componentType.add(new JavaMappedService("service" + count++, serviceContract, mockedInterface));
            }
            
            return componentType;
            
        } catch(ClassNotFoundException ex) {
            throw new LoaderException(ex);
        }
        
    }

}
