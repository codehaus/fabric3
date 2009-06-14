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
 */
package org.fabric3.host;

/**
 * 
 * Namespace URIs used in Fabric3.
 *
 */
public class Namespaces {
	   
    /**
     * Namespace URI used for core Fabric3.
     * Intended usage for map keys in SCDLs etc. Recommended prefix f3-core.
     */
    public static final String CORE = "urn:fabric3.org:core";
   
    /**
     * Namespace URI used for fabric3 binding extensions.
     * Intended usage for non-standard bindings like hessian, burlap, ftp etc, Recommended prefix f3-binding.
     */
    public static final String BINDING = "urn:fabric3.org:binding";
   
    /**
     * Namespace URI used for fabric3 implementation extensions.
     * Intended usage for non-standard implementations like system, groovy, junit etc, Recommended prefix f3-implementation.
     */
    public static final String IMPLEMENTATION = "urn:fabric3.org:implementation";
   
    /**
     * Namespace URI used for fabric3 policy extensions.
     * Intended usage for non-standard SCA intents and policies like dataBinding.jaxb, authorization.message, Recommended prefix f3-policy.
     */
    public static final String POLICY = "urn:fabric3.org:policy";
   
    /**
     * Namespace URI used for other extensions like implementation.cache and implementation.jpa. Recommended prefix f3-other.
     */
    public static final String OTHER = "urn:fabric3.org:other";
   
    /**
     * Namespace URI used for fabric3 maven extensions. Recommended prefix f3-maven.
     */
    public static final String MAVEN = "urn:fabric3.org:maven";
    
    /**
     * Private constructor.
     */
    private Namespaces() {
    }

}
