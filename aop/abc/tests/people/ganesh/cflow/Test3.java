public class Test3 {

    static int x;

    public static void main(String[] args) { 
	x=3;
    }

}


aspect Test3Aspect {

    pointcut pc() : set(int Test3.x);

    before() : pc() && cflow(pc() && cflow(pc()))
    {
	System.out.println("before");
    }

}
