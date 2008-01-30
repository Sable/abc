//Listing 5.1 The Item class: models an item that can be purchased

public class Item {
    private String _id;
    private float _price;

    public Item(String id, float price) {
	_id = id;
	_price = price;
    }

    public String getID() {
	return _id;
    }

    public float getPrice() {
	return _price;
    }

    public String toString() {
	return "Item: " + _id;
    }
}
