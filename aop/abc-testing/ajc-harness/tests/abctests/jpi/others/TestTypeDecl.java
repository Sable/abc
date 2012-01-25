import org.aspectj.testing.Tester;

//jpi int JP(int amount, int l);
//jpi int JP2(int amount, int m) extends JP(amount,m);

jpi int JP(Object a);
jpi int JP2(Object a) extends JP(a);


public class C{

/*    exhibits int JP(int a, int l): args(a,l);
    exhibits int JP2(int a, int l): args(a,l);
    int bar(int x, int l){
        return x;
    }
    public static void main(String[] args){
        new C().bar(6,5);
    }
*/
    exhibits int JP(Object a): this(a) && args(int);
    exhibits int JP2(Object a): this(a) && args(int);
    
    int bar(int x){
    	//Tester.checkEqual(8,x,"we expected 8 and saw "+x);    	
        return x;
    }
    public static void main(String[] args){
        new C().bar(6);
    }
	    
    //public static int foo(int x){return 1;}

}

aspect A{

/*    int around JP(int a, int m){ 
    	Tester.check(false,"should never get executed");
        return 1;
    }
    int around JP2(int a, int h){ 
    	return 1;
    }
*/

    final int around JP(Object a){ 
    	return proceed(a);
    }
    int around JP2(Object a){ 
    	return proceed(a);
    }
	
	
/*	pointcut m(): args(int);
	
	int around(int a) : args(a) && !m(){
		return 1;
	}*/
}

/*jpi void JP(int a);


aspect C{
	
	final void around JP(int m){}
	
	void around JP(int l){
		//int i = new String("");
		//return i;
	}

	void around JP(int l){
		//int i = new String("");
		//return i;
	}
	
	before JP(int m){}
	
	after JP(int a){}
	
	before JP(int n){}
	
	after JP(int h){}
	
	final after JP(int l){}
	public static void main(String[] args){
		
	}
}
*/