//Listing 5.4 ShoppingCartOperator: Manages the shopping cart

public class ShoppingCartOperator {
    public static void addShoppingCartItem(ShoppingCart sc,
					   Inventory inventory,
					   Item item) {
	inventory.removeItem(item);
	sc.addItem(item);
    }

    public static void removeShoppingCartItem(ShoppingCart sc,
					      Inventory inventory,
					      Item item) {
	sc.removeItem(item);
	inventory.addItem(item);
    }
}
