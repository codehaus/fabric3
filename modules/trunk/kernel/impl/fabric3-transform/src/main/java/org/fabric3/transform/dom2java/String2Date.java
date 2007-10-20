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
package org.fabric3.transform.dom2java;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.w3c.dom.Node;

import org.fabric3.scdl.DataType;
import org.fabric3.spi.model.type.JavaClass;
import org.fabric3.spi.transform.TransformContext;
import org.fabric3.spi.transform.TransformationException;
import org.fabric3.transform.AbstractPullTransformer;

/**
 * @version $Rev: 566 $ $Date: 2007-07-24 22:07:41 +0100 (Tue, 24 Jul 2007) $ String format of Date, expects format of
 *          date as dd/mm/yyyy (<date>12/07/2007</date>)
 */
public class String2Date extends AbstractPullTransformer<Node, Date> {

    /**
     * Standard Date Format
     */
    private final DateFormat dateFormatter;

    /**
     * Target Class (Date)
     */
    private static final JavaClass<Date> TARGET = new JavaClass<Date>(Date.class);

    /**
     * Default Constructor
     */
    public String2Date() {
        dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
        dateFormatter.setLenient(false);
    }

    /**
     * @see org.fabric3.spi.transform.Transformer#getTargetType()
     */
    public DataType<?> getTargetType() {
        return TARGET;
    }

    /**
     * @see org.fabric3.spi.transform.PullTransformer#transform(java.lang.Object,org.fabric3.spi.transform.TransformContext)
     *      Applies transformation for Date
     */
    public Date transform(final Node node, final TransformContext context) throws TransformationException {
        try {
            return dateFormatter.parse(node.getTextContent());
        } catch (ParseException pe) {
            throw new TransformationException("Unsupported Date Format ", pe);
		} 
	}

}
