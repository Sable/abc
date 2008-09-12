package test;

public aspect Test58 {
	ast A;
	
	syn int A.c2_a() circular [1]  {
		c2_acount++;
		return c2_b();
	}
	
	syn int A.c2_b() circular [1]  {
		c2_bcount++;
		int c = c2_a();
		return c == 2 ? 2 : c + 1;
	}
	
	syn int A.c3() {
		c3count++;
		return c2_a();
	}
	
	syn int A.c1_a() circular [1]  {
		c1_acount++;
		int c = c1_b() + c3();
		return c >= 5 ? 5 : c + 1;
	}
	
	syn int A.c1_b() circular [1]  {
		c1_bcount++;
		return c1_a();
	}
	
	syn int A.x(boolean b, String y) circular [1] {
		return 1;
	}
	
	public int A.c1_acount;
	public int A.c1_bcount;
	public int A.c3count;
	public int A.c2_acount;
	public int A.c2_bcount;
	
	public static void main(String[] args) {
		System.out.println("58, Circular: no stack of strongly connected components");
		A root = new A();
    System.out.println("Order: c1 before c2");
		System.out.println("c1_a() = " + root.c1_a());
		System.out.println("c2_a() = " + root.c2_a());
		System.out.println("c1_acount " + root.c1_acount);
		System.out.println("c1_bcount " + root.c1_bcount);
		System.out.println("c3count " + root.c3count);
		System.out.println("c2_acount " + root.c2_acount);
		System.out.println("c2_bcount " + root.c2_bcount);
    
		root = new A();
    System.out.println("Order: c2 before c1");
		System.out.println("c2_a() = " + root.c2_a());
		System.out.println("c1_a() = " + root.c1_a());
		System.out.println("c1_acount " + root.c1_acount);
		System.out.println("c1_bcount " + root.c1_bcount);
		System.out.println("c3count " + root.c3count);
		System.out.println("c2_acount " + root.c2_acount);
		System.out.println("c2_bcount " + root.c2_bcount);
	}
}
