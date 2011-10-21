package aspects;

import classes.Item;
import classes.ShoppingSession;
import classes.Utils;
import joinpointinterfaces.*;

public aspect Logger {

	void around Buying(Item it, int amt, double price){
		String message = "Product "+it.getDescription();
		message += " amount: "+amt;
		message += " price : "+price;
	    Utils.RegisterEvent("buying",message);
	    proceed(it,amt,price);
	  }
	
}
