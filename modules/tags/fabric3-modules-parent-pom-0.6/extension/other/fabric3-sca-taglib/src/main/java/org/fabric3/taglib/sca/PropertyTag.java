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
package org.fabric3.taglib.sca;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

/**
 * Tag for declaring properties.
 *
 * @version $Rev$ $Date$
 */
public class PropertyTag extends SimpleTagSupport {
    private String name;
    private Class<?> type;

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) throws JspException {
        try {
            this.type = Class.forName(type, true, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            throw new JspException("Property type not found: " + name, e);
        }
    }

    public void doTag() throws JspException, IOException {
        Object ref = getJspContext().getAttribute(name, PageContext.SESSION_SCOPE);
        if (ref == null) {
            ref = getJspContext().getAttribute(name, PageContext.APPLICATION_SCOPE);
            if (ref == null) {
                throw new JspException("Property not found: " + name);
            }
        }
        if (!(type.isInstance(ref))) {
            throw new JspException("Property " + name + " not of type: " + type.getName());
        }
        getJspContext().setAttribute(name, ref);
    }

}