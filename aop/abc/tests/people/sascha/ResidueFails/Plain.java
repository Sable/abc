public aspect Plain 
{
	public static int n=5;
	int around(Test x): call(int Test.* (..)) && ( (if(false) && target(x)) ) // || (if (true) && this(x))) //&& if(--n>0)
	{
		return proceed(x);
	}
}
