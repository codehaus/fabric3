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
package org.fabric3.loader.composite;

import java.net.URL;

import org.fabric3.spi.loader.LoaderException;

/**
 * @version $Rev$ $Date$
 */
public class ServiceNotFoundException extends LoaderException {
    private static final long serialVersionUID = -7651247438850248926L;

    public ServiceNotFoundException(String name, URL resourceLocation) {
        super("Service not found", name);
        if (resourceLocation != null) {
            // FIXME create typed ctor param for LoaderException in next SPI rev
            setResourceURI(resourceLocation.toString());
        }
    }
}