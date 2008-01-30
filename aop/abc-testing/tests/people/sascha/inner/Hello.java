
public class Hello
{  
	public static void main(String args[])
	{
		new Hello().around(0);
	}
	int around(final int i)
	{
		class LocalClass 
		{
			class InnerClass 
			{
				int run () 
				{
					class LocalClassNeverUsed
					{
						int run () 
						{
							return i;
						}
					}
					return new LocalClass().run();
				}
			}
			int run () 
			{
				return new InnerClass().run();
			}			
		};
		return new LocalClass().run();
	}
}