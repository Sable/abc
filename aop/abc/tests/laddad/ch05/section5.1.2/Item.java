//Listing 5.6 The Item class with logging enabled

import java.util.logging.*;

public class Item {
    private String _id;
    private float _price;

    static Logger _logger = Logger.getLogger("trace");

    public Item(String id, float price) {
	_id = id;
	_price = price;
    }

    public String getID() {
	_logger.logp(Level.INFO, "Item", "getID", "Entering");
	return _id;
    }

    public float getPrice() {
	_logger.logp(Level.INFO, "Item", "getPrice", "Entering");
	return _price;
    }

    public String toString() {
	_logger.logp(Level.INFO, "Item", "toString", "Entering");
	return "Item: " + _id;
    }
}
