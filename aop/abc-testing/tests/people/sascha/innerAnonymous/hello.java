


class Hello
{  
	public static void main(String args[])
	{
		
		Test test=new Test();
		test.IntMethod("s1", new Integer(2), new Integer(3));
		test.IntMethod(new Integer(1), "s2", new Integer(3));
		test.IntMethod(new Integer(1), new Integer(2), "s3");
	}
}
class Test 
{
	public int IntMethod(Object arg1, Object arg2, Object arg3)
	{
		System.out.println("Args: " + arg1 + ":" + arg2 + ":" + arg3);
		return 0;
	}
}