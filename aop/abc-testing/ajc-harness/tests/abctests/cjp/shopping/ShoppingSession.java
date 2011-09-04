class ShoppingSession {
	int totalAmount = 0;
	ShoppingCart sc = new ShoppingCart();

	void buy(final Item item, int amount) {
		Category category = Database.categoryOf(item);
		totalAmount = exhibit								
			BonusProgram.Buying(int amount, Category c) {
				sc.add(item, amount);		
				return totalAmount + amount;				
	    	} (amount,category); 							
	}
}



aspect BonusProgram {
	jpi int Buying(int amount, Category cat);
	int around Buying(int amt, Category cat) {	
		if(cat==Item.BOOK)
			amt += amt / 2;
		return proceed(amt, cat);
	}
}			

class ShoppingCart {	
	void add(Item i, int amount) {}
}

class Category {
	
}

class Item {	
	static Category BOOK;
}

class Database {
	static Category categoryOf(Item i) {
		return Item.BOOK;
	}	
}