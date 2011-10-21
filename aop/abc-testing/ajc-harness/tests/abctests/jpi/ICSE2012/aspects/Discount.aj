package aspects;

import classes.BestSeller;
import classes.ShoppingSession;
import classes.Utils;
import classes.EcoFriendly;
import classes.Item;
import joinpointinterfaces.*;

public aspect Discount {
	
	final void around Buying(Item it, int amt, double price){
	    double factor = (amt>5) ? 0.9 : 1;
	    proceed(it,amt,price*factor);
	}	

	void around BuyingBestSeller(Item it, int amt, double price, ShoppingSession ss){
		Utils.SendBestSellerCoupon(ss.getCustomer(), it);
	    proceed(it,amt,price,ss);
	}

	void around BuyingEcoFriendly(Item it, int amt, double price, ShoppingSession ss){
		Utils.SendEcoFriendlyCoupon(ss.getCustomer(), it);
		proceed(it,amt,price,ss);
	}
}
