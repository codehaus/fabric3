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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.host;

/**
 * The root unchecked exception for the fabric3 runtime.
 *
 * @version $Rev$ $Date$
 */

public abstract class Fabric3RuntimeException extends RuntimeException {
    private static final long serialVersionUID = -759677431966121786L;
    private final String identifier;

    /**
     * Override constructor from RuntimeException.
     *
     * @see RuntimeException
     */
    public Fabric3RuntimeException() {
        super();
        this.identifier = null;
    }

    /**
     * Override constructor from RuntimeException.
     *
     * @param message passed to RuntimeException
     * @see RuntimeException
     */
    public Fabric3RuntimeException(String message) {
        super(message);
        this.identifier = null;
    }


    /**
     * Override constructor from Exception.
     *
     * @param message    passed to Exception
     * @param identifier additional error information referred to in the error message
     * @see Exception
     */
    protected Fabric3RuntimeException(String message, String identifier) {
        super(message);
        this.identifier = identifier;
    }

    /**
     * Override constructor from RuntimeException.
     *
     * @param message passed to RuntimeException
     * @param cause   passed to RuntimeException
     * @see RuntimeException
     */
    public Fabric3RuntimeException(String message, Throwable cause) {
        super(message, cause);
        this.identifier = null;
    }


    /**
     * Override constructor from Exception.
     *
     * @param message    passed to Exception
     * @param identifier additional error information referred to in the error message
     * @param cause      passed to RuntimeException
     * @see Exception
     */
    protected Fabric3RuntimeException(String message, String identifier, Throwable cause) {
        super(message, cause);
        this.identifier = identifier;
    }

    /**
     * Override constructor from RuntimeException.
     *
     * @param cause passed to RuntimeException
     * @see RuntimeException
     */
    public Fabric3RuntimeException(Throwable cause) {
        super(cause);
        this.identifier = null;
    }

    /**
     * Returns a string representing additional error information referred to in the error message.
     *
     * @return additional error information
     */
    public String getIdentifier() {
        return identifier;
    }

}
