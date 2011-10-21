package classes;

public class Movie implements BestSeller, Item{

	private String name;
	private double price;
	
	public Movie(String name, double price){
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
