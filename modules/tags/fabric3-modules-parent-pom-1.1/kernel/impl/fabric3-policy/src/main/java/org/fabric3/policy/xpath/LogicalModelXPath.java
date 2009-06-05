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
package org.fabric3.policy.xpath;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jaxen.BaseXPath;
import org.jaxen.Context;
import org.jaxen.JaxenException;
import org.jaxen.SimpleFunctionContext;
import org.jaxen.SimpleNamespaceContext;
import org.oasisopen.sca.Constants;

import org.fabric3.spi.model.instance.LogicalComponent;

/**
 * An XPath implementation based on Jaxen that traverses the domain logical model and matches XPath expressions against it.
 */
public class LogicalModelXPath extends BaseXPath {
    private static final long serialVersionUID = 7175741342820843731L;

    /**
     * Constructor.
     *
     * @param xpathExpr The XPath expression to evaluate against the domain logical model
     * @throws JaxenException if there is a syntax error while parsing the expression
     */
    public LogicalModelXPath(String xpathExpr) throws JaxenException {
        super(xpathExpr, LogicalModelNavigator.getInstance());
        // setup namespaces and functions
        SimpleNamespaceContext nc = new SimpleNamespaceContext();
        nc.addNamespace("sca", Constants.SCA_NS);
        setNamespaceContext(nc);
        SimpleFunctionContext fc = new SimpleFunctionContext();
        fc.registerFunction(Constants.SCA_NS, "URIRef", new UriRefFunction());
        fc.registerFunction(Constants.SCA_NS, "OperationRef", new OperationRefFunction());
        setFunctionContext(fc);
    }

    public Object evaluate(Object node) throws JaxenException {
        Object result = super.evaluate(node);
        if (result instanceof LogicalComponent) {
            return result;
        } else if (result instanceof Collection) {
            List<Object> newList = new ArrayList<Object>();
            for (Object member : ((Collection) result)) {
                newList.add(member);
            }
            return newList;
        }
        return result;
    }

    protected Context getContext(Object node) {
        if (node instanceof Context) {
            return (Context) node;
        }
        if (node instanceof LogicalComponent) {
            return super.getContext(node);
        }

        if (node instanceof List) {
            List<Object> newList = new ArrayList<Object>();

            for (Object o : ((List) node)) {
                newList.add(o);
            }

            return super.getContext(newList);
        }
        return super.getContext(node);
    }

}