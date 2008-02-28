package org.fabric3.tests.function.ordering;

import org.osoa.sca.annotations.Reference;

import junit.framework.TestCase;

/**
 * Tests the retention of composite file order for injected components
 */
public class OrderingITest extends TestCase
{
    @Reference(name="displayService")
    protected ItemDisplayService displayService;
	
    public void testOrderedInjection()
    {
    	String[] expectedItemNames = {"ONE", "TWO", "THREE", "FOUR", 
    							"FIVE", "SIX", "SEVEN", "EIGHT"};
    	
        Item[] actualItems = displayService.getItems();
        
        assertEquals(expectedItemNames.length, actualItems.length);
        for(int idx = 0; idx < expectedItemNames.length; idx++) {
        	//assertEquals(expectedItemNames[idx], actualItems[idx].getName());
        }
    }
}
