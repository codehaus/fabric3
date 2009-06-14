/*
 * Fabric3
 * Copyright (C) 2009 Metaform Systems
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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.model.type.definitions;

import java.util.Set;
import javax.xml.namespace.QName;

import org.oasisopen.sca.Constants;

/**
 * Represents a registered intent within the domain.
 * 
 * @version $Revision$ $Date$
 *
 */
public final class Intent extends AbstractDefinition {

    /** Binding QName */
    public static final QName BINDING = new QName(Constants.SCA_NS, "binding");
    
    /** Implementation QName */
    public static final QName IMPLEMENTATION = new QName(Constants.SCA_NS, "implementation");
    
    /** Intent type. */
    private IntentType intentType;
    
    /** Name of the qualifiable intent if this is a qualified intent . */
    private QName qualifiable;
    
    /** Whether this intent requires other intents. */
    private Set<QName> requires;
    
    /** Constrained type. */
    private QName constrains;

    /**
     * Initializes the name, description and the constrained artifacts.
     * 
     * @param name Name of the intent.
     * @param description Description of the intent.
     * @param constrains SCA artifact constrained by this intent.
     * @param requires The intents this intent requires if this is a profile intent.
     */
    public Intent(QName name, String description, QName constrains, Set<QName> requires) {
        
        super(name);
        
        if(constrains != null) {
            if(!BINDING.equals(constrains) && !IMPLEMENTATION.equals(constrains)) {
                throw new IllegalArgumentException("Intents can constrain only bindings or implementations");
            }
            intentType = BINDING.equals(constrains) ? IntentType.INTERACTION : IntentType.IMPLEMENTATION;
            this.constrains = constrains;
        }
        
        String localPart = name.getLocalPart();
        if(localPart.indexOf('.') > 0) {
            String qualifiableName = localPart.substring(0, localPart.indexOf('.') + 1);
            qualifiable = new QName(name.getNamespaceURI(), qualifiableName);
        }
        
        this.requires = requires;
        
    }
    
    /**
     * Checks whether this is a profile intent.
     * 
     * @return True if this is a profile intent.
     */
    public boolean isProfile() {
        return requires != null && requires.size() > 0;
    }
    
    /**
     * The intents this intent requires if this is a profile intent.
     * 
     * @return Required intents for a profile intent.
     */
    public Set<QName> getRequires() {
        return requires;
    }
    
    /**
     * Checks whether this is a qualified intent.
     * 
     * @return True if this is a qualified intent.
     */
    public boolean isQualified() {
        return qualifiable != null;
    }
    
    /**
     * Returns the qualifiable intent if this is qualified.
     * 
     * @return Name of the qualifiable intent.
     */
    public QName getQualifiable() {
        return qualifiable;
    }
    
    /**
     * Returns the type of this intent.
     * 
     * @return Type of the intent.
     */
    public IntentType getIntentType() {
        return intentType;
    }
    
    /**
     * Whether this intent constrains the specified type.
     * 
     * @param type Type of the SCA artifact.
     * @return True if this intent constrains the specified type.
     */
    public boolean doesConstrain(QName type) {
        return type.equals(constrains);
    }

}
