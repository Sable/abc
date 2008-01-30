public class Foo
{  
	public static void main(String args[])
	{
		new Foo().foo("first", null);
		new Foo().foo(null, "second");
	}
	public void foo(Object ob1, Object ob2) {
		System.out.println(ob1);
		System.out.println(ob2);
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
