package classes;

public class Customer {
	
	private String name;
	private String email;
	
	public Customer(String name, String email) {
		super();
		this.name = name;
		this.email = email;
	}
	
	public String getEmail(){ return this.email;}
	public String getInformation(){ return this.name;}

}
