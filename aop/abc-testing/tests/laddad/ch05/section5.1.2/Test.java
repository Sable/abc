//Listing 5.8 The Test class with logging enabled

import java.util.logging.*;

public class Test {
    static Logger _logger = Logger.getLogger("trace");

    public static void main(String[] args) {
	_logger.logp(Level.INFO,
		     "Test", "main", "Entering");
	Inventory inventory = new Inventory();
	Item item1 = new Item("1", 30);
	Item item2 = new Item("2", 31);
	Item item3 = new Item("3", 32);
	inventory.addItem(item1);
	inventory.addItem(item2);
	inventory.addItem(item3);
	ShoppingCart sc = new ShoppingCart();
	ShoppingCartOperator.addShoppingCartItem(sc, inventory, item1);
	ShoppingCartOperator.addShoppingCartItem(sc, inventory, item2);
    }
}
