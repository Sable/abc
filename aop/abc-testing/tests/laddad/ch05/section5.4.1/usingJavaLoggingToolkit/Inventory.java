//Listing 5.3 The Inventory class: models the shop inventory

import java.util.*;

public class Inventory {
    private List _items = new Vector();

    public void addItem(Item item) {
	_items.add(item);
    }

    public void removeItem(Item item) {
	_items.remove(item);
    }
}
