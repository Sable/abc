package classes;

public class Book implements BestSeller, EcoFriendly, Item{
	
	private String name;
	private double price;
	
	public Book(String name, double price){
		this.name = name;
		this.price = price;
	}

	public String getDescription() {
		return this.name;
	}
	
	public double getPrice(){
		return this.price;
	}
	
	

}
