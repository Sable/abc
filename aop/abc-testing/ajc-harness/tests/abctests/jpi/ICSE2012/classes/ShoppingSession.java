package classes;

import java.util.*;
import joinpointinterfaces.*;

public class ShoppingSession {
	
	private Set<Item> items;
	private Customer cus;
	
	exhibits void Buying(Item it, int amt, double price):
	    execution(void buy(..))
	    && args(it,amt,price);	

	exhibits void BuyingEcoFriendly(Item it, int amt, double price, ShoppingSession ss):
	    execution(void buy(..))
	    && args(it,amt,price)	
		&& this(ss)
		&& args(EcoFriendly,..);

	exhibits void BuyingBestSeller(Item it, int amt, double price, ShoppingSession ss):
	    execution(void buy(..))
	    && args(it,amt,price)
		&& this(ss)
		&& args(BestSeller,..);
	
	public ShoppingSession(Customer acus){
		items = new HashSet<Item>();
		this.cus = acus;
	}
	
	public Customer getCustomer(){ return cus;}
	
	public void buy(Item it, int amount, double price){
		items.add(it);
		System.out.println("Buying "+amount+" item: "+it.getDescription()+" with total price: "+price*amount+"\n");
	}
	
	public static void main(String[] args){
		Customer cus = new Customer("milton", "minostro at dcc_dot_uchile_dot_cl");
		ShoppingSession sp = new ShoppingSession(cus);		
		Book b1 = new Book("book 1", 3500);
		Book b2 = new Book("book 2", 500);
		Movie m1 = new Movie("movie 1", 1500);	
		sp.buy((Item)b1,5,b1.getPrice());
		sp.buy((Item)b2,8,b2.getPrice());
		sp.buy((Item)m1,10,m1.getPrice());
	}

}