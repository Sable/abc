
aspect Plain0 {
	//declare precedence : Plain4, Plain3, Plain0;
	int around() :  call(int Test.* (..)) //adviceexecution() && within(Plain2)
	{
		class LocalClass2 
		{
			int run() 
			{
				System.out.println("42");
				return proceed();
			}
		}
		
		Runnable lc=new Runnable() {
			public void run() 
			{
				new LocalClass2().run();
			}
		};
		lc.run();
		return 0;
	}
}
