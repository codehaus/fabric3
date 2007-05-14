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
package org.fabric3.scdl4j;

import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;

import org.scdl4j.Component;
import org.scdl4j.Composite;

/**
 * @version $Rev$ $Date$
 */
public class CompositeImpl implements Composite {
    private final SCDL4JImpl scdl4j;
    private final QName name;
    private boolean autowire;
    private Map<String, ComponentImpl> components = new HashMap<String, ComponentImpl>();

    public CompositeImpl(SCDL4JImpl scdl4j, QName name) {
        this.scdl4j = scdl4j;
        this.name = name;
    }

    public Composite include(Composite include) {
        return this;
    }

    public Component addComponent(String name) {
        ComponentImpl component = new ComponentImpl(scdl4j, name);
        components.put(name, component);
        return component;
    }

    public QName getName() {
        return name;
    }

    public boolean isAutowire() {
        return autowire;
    }

    public void setAutowire(boolean autowire) {
        this.autowire = autowire;
    }
}
