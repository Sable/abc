package p;
public class Inner
{
	
	/** Comment */
	private A a;

	public void bar0(){
		class Local{
			public void run()
			{
				System.out.println(/*///Inner.this.*/a.bar2());
				/*///Inner.this.*/a.bar3= "fred";
			}
		}
	}
	
	public void bar()
	{
		new Runnable()
		{
			public void run()
			{
				System.out.println(/*///Inner.this.*/a.bar2());
				/*///Inner.this.*/a.bar3= "fred";
			}
		};
	}
	
	class InnerInner{
		public void run()
		{
			System.out.println(/*///Inner.this.*/a.bar2());
			/*///Inner.this.*/a.bar3= "fred";
		}
	}

	/**
	 * @param a
	 */
	public ///
	Inner(A a0) {
		this.a = a0;
	}
}