/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
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