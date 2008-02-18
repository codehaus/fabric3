package org.fabric3.tests.function.ordering;

import java.util.List;

import org.osoa.sca.annotations.Reference;

public class OrderedDisplayService implements ItemDisplayService{
	private List<Item> items;

    public OrderedDisplayService(@Reference(name = "items")List<Item> items) {
		this.items = items;
    }

    public Item[] getItems() {
		return items.toArray(new Item[0]);
    }
}
