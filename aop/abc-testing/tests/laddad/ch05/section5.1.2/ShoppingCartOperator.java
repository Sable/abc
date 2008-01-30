//Listing 5.4 ShoppingCartOperator: manages the shopping cart

import java.util.logging.*;

public class ShoppingCartOperator {
    static Logger _logger = Logger.getLogger("trace");

    public static void addShoppingCartItem(ShoppingCart sc,
					   Inventory inventory,
					   Item item) {
	_logger.logp(Level.INFO, "ShoppingCartOperator", "addShoppingCartItem"
		     , "Entering");
	inventory.removeItem(item);
	sc.addItem(item);
    }

    public static void removeShoppingCartItem(ShoppingCart sc,
					      Inventory inventory,
					      Item item) {
	_logger.logp(Level.INFO, "ShoppingCartOperator"
		     , "removeShoppingCartItem" , "Entering");
	sc.removeItem(item);
	inventory.addItem(item);
    }
}
