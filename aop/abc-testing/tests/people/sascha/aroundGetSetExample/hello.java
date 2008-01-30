
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
		i=4;
		//d=3.0;
		d*=2;
		h=new Hello();
		int j=i;
		j+=k;
		//double c=d;
		//c+=4.0;
		Object ob=h;
		if (ob!=null) 
		{
			j+=1;
		}
		return j;
	}
}
