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
 */
package org.fabric3.jaxb.runtime.spi;

import javax.xml.bind.JAXBContext;

import org.fabric3.spi.transform.PullTransformer;

/**
 * Creates transformers for marshalling between JAXB objects and a data type.
 *
 * @version $Revision$ $Date$
 */
public interface DataBindingTransformerFactory<TYPE> {

    /**
     * Creates a transformer from the data type to a JAXB type.
     *
     * @param context the JAXB context to use for transformation
     * @return the transformer
     */
    PullTransformer<TYPE, Object> createToJAXBTransformer(JAXBContext context);

    /**
     * Creates a transformer from a JAXB type to the data type.
     *
     * @param context the JAXB context to use for transformation
     * @return the transformer
     */
    PullTransformer<Object, TYPE> createFromJAXBTransformer(JAXBContext context);

}
