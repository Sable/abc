
aspect Plain0 {
	declare precedence : Plain4, Plain3, Plain0;
	void around() :  adviceexecution() && within(Plain2)
	{
		class LocalClass {
			LocalClass() 
			{
				proceed();
			}
			
		};
		LocalClass lc=new LocalClass();
	}
}
/*
aspect Plain2 {
	int around() :  adviceexecution() && within(Plain)
	{
		System.out.println("before proceed 2");
		int result=proceed();
		System.out.println("after proceed 2");
		return result;
	}
}
*/
/*
public aspect Plain 
{
	public static int n=5;
	int around(String x): call(int Test.* (..)) && (args(x, *, *) || args(*, x, *) || args( *, *,x)   )
	{
		//System.out.println("before proceed: " + n);
		int i=proceed("proceed");
		//System.out.println("after proceed: "  + n);
		return i;
	}
}*/
/*
 error:
public aspect Plain 
{
	public static int n=5;
	int around(Test x): call(int Test.* (..)) && ( (if(false) && target(x)) || (if (true) && this(x))) //&& if(--n>0)
	{
		System.out.println("before proceed: " + n);
		Test y=new Test();
		y.me="proceed";
		int i=proceed(y);
		System.out.println("after proceed: "  + n);
		return i;
	}
}
*/
/*
public aspect Plain 
{
	public static int n=5;
	int around(): (call(int Test.* (..)) || (adviceexecution() && !cflowbelow(adviceexecution()))) //&& if(--n>0)
	{
		System.out.println("before proceed: " + n);
		int i=proceed();
		System.out.println("after proceed: "  + n);
		return i;
	}
}*/
/*
aspect Plain3 {
	int around() :  adviceexecution() && within(Plain2)
	{
		System.out.println("before proceed 3");
		int result=proceed();
		System.out.println("after proceed 3");
		return result;
	}
}

aspect Plain4 {
	int around() :  adviceexecution() && within(Plain2)
	{
		System.out.println("before proceed 4");
		int result=proceed();
		System.out.println("after proceed 4");
		return result;
	}
}*/