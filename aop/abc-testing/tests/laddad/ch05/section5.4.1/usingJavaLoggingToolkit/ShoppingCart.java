//Listing 5.2 The ShoppingCart class: models a shopping cart

import java.util.*;

public class ShoppingCart {
    private List _items = new Vector();

    public void addItem(Item item) {
	_items.add(item);
    }

    public void removeItem(Item item) {
	_items.remove(item);
    }

    public void empty() {
	_items.clear();
    }

    public float totalValue() {
	// unimplemented... free!
	return 0;
    }
}
