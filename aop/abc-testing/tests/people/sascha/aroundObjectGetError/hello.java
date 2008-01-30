
class Hello
{  
	public static void main(String args[])
	{
		Test test=new Test();
		int i=test.test();
	}
}
class Test 
{
	int i;
	int k;

	double d;

	Hello h;

	public int test()
	{
		h=new Hello();
		int j=i;
		j+=k;
		double c=d;
		c+=4.0;
		Object ob=h;
		if (ob!=null) 
		{
			c+=1.0;
		}
		return j+(int)c;
	}
}
