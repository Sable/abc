package test;

public aspect Test63 {
	ast A;
	 

	  syn int A.a(String name) circular [1] {
	  	if (a(1) >= 2)
	  		return 3;
	  	else
	  		return a(1) +1;
	  }
	  
	 
	  
	  syn int A.a(int i) circular  [1]  {
	  		return a("x") + b();
	  }
	  
	  syn int A.b() = c1();
	  
	  syn int A.c1() circular [0] = c2();
	  syn int  A.c2() circular [0] = c1() +a(1);
	  

	  public static void main(String[] args) {
	  	 System.out.println("Circular: two mutually circular paramterized attributes");
	     A root  = new A();
	     try{
	     System.out.println("a(\"x\") == 3: " + (root.a("x") == 3));
	     System.out.println("a(1) == 3: " + (root.a(1) == 3));
	     }
	     catch (RuntimeException e) {
	     	System.out.println("RuntimeException!");
	     }
	    
	  }
	
	
	
	
	
	
	
	
}
