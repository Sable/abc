class Hello
{  
	public static void main(String args[])
	{
		Test test=new Test();
		test.me="main";
		test.doIt();
	}
	
}
class Test 
{
	public void doIt () 
	{
		IntMethod(me,3.0,4);
	}
	String me="original";
	public int IntMethod(String s, double d, int i)
	{
		System.out.println("IntMethod, target: " + me + " this: " + s);
		return 0;
	}
}