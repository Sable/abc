public class Foo
{  
    public static void main(String args[])
    {       
    }
}
aspect Aspect 
{
    public static int n=3;
    void around(): execution(* Foo.* (..)) || 
            (adviceexecution() && if(--n>0)) 
    {
        System.out.println("n=" + n);
		if (n!=0)
			throw new RuntimeException();
        proceed();
    }
}
