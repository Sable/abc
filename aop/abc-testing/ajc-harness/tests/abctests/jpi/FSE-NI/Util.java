package Util;

import java.util.HashMap;
import java.lang.*;

public class Item{
	
	public static Item factory(){
		return new Item();
	}
	
	public static int getPrice(){return 100;}
} 

public class Customer{
	
	boolean birthday;
	
	public Customer(boolean b){birthday = b;}
	
	public static Customer factory(boolean birthday){
		return new Customer(birthday);
	}
	
	public boolean hasBirthday(){return birthday;}
}

public class Invoice{
	public void add(Item i, int a, Customer c){}
}

public class ShoppingCart{
	HashMap<Item,Float> products = new HashMap<Item,Float>();
	
	public void add(Item i, float price, int a){
		products.put(i,price*(float)a);
	}
	
	public float get(Item i){return (float)products.get(i);}
}