/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 * --- Original Apache License ---
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
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
package org.fabric3.model.type.component;

import java.net.URL;
import javax.xml.namespace.QName;

import org.fabric3.model.type.ModelObject;

/**
 * Model object that represents the include of a composite by value.
 *
 * @version $Rev: 5481 $ $Date: 2008-09-26 02:36:30 -0700 (Fri, 26 Sep 2008) $
 */
public class Include extends ModelObject {
    private static final long serialVersionUID = 3982129607792011105L;
    private QName name;
    private URL scdlLocation;
    private Composite included;

    /**
     * Returns the name of the composite that is being included.
     *
     * @return the name of the composite that is being included
     */
    public QName getName() {
        return name;
    }

    /**
     * Sets the name of the composite that is being included.
     *
     * @param name the name of the composite that is being included
     */
    public void setName(QName name) {
        this.name = name;
    }

    /**
     * Returns the location of the SCDL for composite being included.
     *
     * @return the location of the SCDL for composite being included
     */
    public URL getScdlLocation() {
        return scdlLocation;
    }

    /**
     * Sets the location of the SCDL for composite being included.
     *
     * @param scdlLocation the location of the SCDL for composite being included
     */
    public void setScdlLocation(URL scdlLocation) {
        this.scdlLocation = scdlLocation;
    }

    /**
     * Returns the composite that was included.
     *
     * @return the composite that was included
     */
    public Composite getIncluded() {
        return included;
    }

    /**
     * Sets the composite that was included.
     *
     * @param included the composite that was included
     */
    public void setIncluded(Composite included) {
        this.included = included;
    }

}
