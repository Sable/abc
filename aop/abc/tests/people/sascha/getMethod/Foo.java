
public class Foo 
{
	public static void main(String[] args) 
	{
		new Foo().bar();
	}
	void bar() 
	{
		System.out.println("bar");
	}
}

aspect Aspect 
{
	void around(final Object ob) : call(void *.bar(..)) && target(ob)
	{
		class FirstDegree implements Runnable {
			public void run() { proceed(ob); }        
		}
		Runnable r=new FirstDegree();
		r.run();
	}
}