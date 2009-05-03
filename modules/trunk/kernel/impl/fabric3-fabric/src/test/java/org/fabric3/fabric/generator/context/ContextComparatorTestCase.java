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
package org.fabric3.fabric.generator.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.fabric3.fabric.command.StartContextCommand;
import org.fabric3.spi.command.Command;

/**
 * @version $Revision$ $Date$
 */
public class ContextComparatorTestCase extends TestCase {

    public void testOrder() throws Exception {
        QName oneName = new QName(null, "one");
        QName twoName = new QName(null, "two");
        QName threeName = new QName(null, "three");
        List<Command> list = new ArrayList<Command>();
        StartContextCommand one = new StartContextCommand(oneName);
        StartContextCommand two = new StartContextCommand(twoName);
        StartContextCommand three = new StartContextCommand(threeName);
        list.add(three);
        list.add(two);
        list.add(one);

        Map<QName, Integer> order = new HashMap<QName, Integer>();
        order.put(oneName, 1);
        order.put(threeName, 3);
        order.put(twoName, 2);

        ContextComparator comparator = new ContextComparator(order);
        Collections.sort(list, comparator);

        assertEquals(one, list.get(0));
        assertEquals(two, list.get(1));
        assertEquals(three, list.get(2));
    }


}
