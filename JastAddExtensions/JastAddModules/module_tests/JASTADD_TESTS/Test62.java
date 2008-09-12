package test;

public aspect Test62 {
	ast A ::= B;
	  ast B;

	  syn boolean A.a(String name) circular [false] {
	  	return (a(1) == 1);
	  }
	  
	  //syn boolean A.a(int i) circular [1] = 1;
	  
	  syn int A.a(int i) circular  [1] = 1;
	  

	  public static void main(String[] args) {
	  	 System.out.println("Circular: two mutually circular paramterized attributes");
	     A root  = new A();
	     System.out.println("a(\"x\") == true: " + (root.a("x") == true));
	     System.out.println("a(1) == 1: " + (root.a(1) == 1));
	    
	  }
	
	
	
	
	
	
	
	
}
