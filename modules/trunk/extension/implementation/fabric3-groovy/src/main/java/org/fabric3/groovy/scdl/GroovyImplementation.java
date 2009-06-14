/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.groovy.scdl;

import javax.xml.namespace.QName;

import org.fabric3.model.type.component.Implementation;
import org.fabric3.pojo.scdl.PojoComponentType;

/**
 * A component implemented in Groovy. The implementation can be a script in source or compiled form.
 *
 * @version $Rev$ $Date$
 */
public class GroovyImplementation extends Implementation<PojoComponentType> {
    private static final long serialVersionUID = -8092204063300139457L;
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
