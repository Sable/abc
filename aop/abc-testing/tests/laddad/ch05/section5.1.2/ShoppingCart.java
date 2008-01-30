//Listing 5.7 The ShoppingCart class with logging enabled

import java.util.*;
import java.util.logging.*;

public class ShoppingCart {
    static Logger _logger = Logger.getLogger("trace");

    private List _items = new Vector();

    public void addItem(Item item) {
	_logger.logp(Level.INFO,
		     "ShoppingCart", "addItem", "Entering");
	_items.add(item);
    }

    public void removeItem(Item item) {
	_logger.logp(Level.INFO,
		     "ShoppingCart", "removeItem", "Entering");
	_items.remove(item);
    }

    public void empty() {
	_logger.logp(Level.INFO,
		     "ShoppingCart", "empty", "Entering");
	_items.clear();
    }

    public float totalValue() {
	_logger.logp(Level.INFO,
		     "ShoppingCart", "totalValue", "Entering");
	// unimplemented... free!
	return 0;
    }
}
