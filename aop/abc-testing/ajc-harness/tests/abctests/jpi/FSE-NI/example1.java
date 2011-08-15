import Util.*;
import org.aspectj.testing.Tester;


jpi void CheckingOut(Item item, float price, int amount, Customer cus);

public class ShoppingSession{
	
	exhibits void CheckingOut(Item i, float price, int amount, Customer c):
		execution(* checkOut(..)) && args(i,price,amount,c);
	
	ShoppingCart sc = new ShoppingCart();
	Invoice inv = new Invoice();
	
	public void checkOut(Item item, float price, int amount, Customer cus){
		if (cus.hasBirthday()){
			Tester.check(Item.getPrice() > price,"price must be less than PRICE "+price);	
		}
		else{
			Tester.check(Item.getPrice() == price,"price must be equal to PRICE "+price);			
		}
		sc.add(item, price, amount);
		inv.add(item, amount, cus);
	}
	
	public static void main(String[] args){
		ShoppingSession sp = new ShoppingSession();		
		sp.checkOut(Item.factory(),Item.getPrice(),2,Customer.factory(false));
		sp.checkOut(Item.factory(),Item.getPrice(),2,Customer.factory(true));
	}
}

aspect Discount{
	void around CheckingOut(Item item, float price, int amount, Customer cus){
		double factor = cus.hasBirthday() ? 0.95 : 1;
		proceed(item, (float)(price*factor), amount, cus);
	}
}