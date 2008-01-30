//Listing 5.3 The Inventory class: models the shop inventory

import java.util.*;
import java.util.logging.*;

public class Inventory {
    private List _items = new Vector();

    static Logger _logger = Logger.getLogger("trace");

    public void addItem(Item item) {
	_logger.logp(Level.INFO, "Inventory", "addItem", "Entering");
	_items.add(item);
    }

    public void removeItem(Item item) {
	_logger.logp(Level.INFO, "Inventory", "removeItem", "Entering");
	_items.remove(item);
    }
}
