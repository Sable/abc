//Listing 6.3 The Product class, with an aspect that controls its creation

public class Product {
    public Product() {
	// constructor implementation
    }

    // product methods
    static aspect FlagNonFactoryCreation {
	declare error
	    : call(Product.new(..))
	    && !within(ProductFactory+)
	    : "Only ProductFactory can create Product";
    }
}
