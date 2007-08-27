/*
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
package org.fabric3.binding.ejb.spi;

import javax.naming.NamingException;

/**
 * Various application servers may have different mechanisms for resolving ejb-links.
 * This interface is intended to allow users to hook into a particular app server's
 * resolution mechanism.
 *
 * @version $Revision: 1 $ $Date: 2007-05-14 10:40:37 -0700 (Mon, 14 May 2007) $
 */
public interface EjbLinkResolver {

    public Object resolveEjbLink(String ejbLink, String interfaceName) throws EjbLinkException;
    public void registerEjbLink(String ejbLink, Object ejb) throws EjbLinkException;    

}
