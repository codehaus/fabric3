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
package org.fabric3.groovy.scdl;

import javax.xml.namespace.QName;

import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.Implementation;

/**
 * A component implemented in Groovy. The implementation can be a script in source or compiled form.
 *
 * @version $Rev$ $Date$
 */
public class GroovyImplementation extends Implementation<PojoComponentType> {
    public static final QName IMPLEMENTATION_GROOVY = new QName("http://www.fabric3.org/xmlns/groovy/1.0", "groovy");

    private String scriptName;
    private String className;

    public GroovyImplementation() {
    }

    public GroovyImplementation(String scriptName, String className) {
        this.scriptName = scriptName;
        this.className = className;
    }

    public GroovyImplementation(String scriptName, String className, PojoComponentType componentType) {
        super(componentType);
        this.scriptName = scriptName;
        this.className = className;
    }

    public QName getType() {
        return IMPLEMENTATION_GROOVY;
    }

    /**
     * Returns the name of a file containing the script source.
     *
     * @return the name of a file containing the script source
     */
    public String getScriptName() {
        return scriptName;
    }

    /**
     * Sets the name of a file containing the script source.
     *
     * @param scriptName the name of a file containing the script source
     */
    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    /**
     * Returns the name of a compiled Groovy class.
     *
     * @return the name of a compiled Groovy class
     */
    public String getClassName() {
        return className;
    }

    /**
     * Sets the name of a compiled Groovy class.
     *
     * @param className the name of a compiled Groovy class
     */
    public void setClassName(String className) {
        this.className = className;
    }
}
