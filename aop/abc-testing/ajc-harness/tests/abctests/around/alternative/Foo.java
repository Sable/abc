
public class Foo
{  
	public static void main(String args[])
	{
		new Foo().foo("string", null);
		new Foo().foo2(null, "string");
	}
	public void foo(Object ob1, Object ob2) 
	{
		System.out.println(ob1 + ", " + ob2);
		if (!ob1.equals("new") || ob2!=null)
			throw new RuntimeException();
	}
	public void foo2(Object ob1, Object ob2) 
	{
		System.out.println(ob1 + ", " + ob2);
		if (!ob2.equals("new") || ob1!=null)
			throw new RuntimeException();
	}
}


aspect Aspect
{
    void around(String s): 
        call(void *.foo*(..)) &&         
        (args(s,..) ||  args(.., s))
    {
        proceed("new");
    }
}