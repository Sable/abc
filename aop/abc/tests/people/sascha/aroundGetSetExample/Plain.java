
public aspect Plain {

	//int i;

	int around(): get(int *.*)
	{
		System.out.println("int: before proceed");
		int result=proceed();
		System.out.println("int: after proceed");
		return result;
	}
	/*void around(): set(int *.*)
	{
		System.out.println("int, set: before proceed");
		proceed();
		System.out.println("int, set: after proceed");
		return;
	}*/
	void around(): set(double *.*)
	{
		System.out.println("double, set: before proceed");
		proceed();
		System.out.println("double, set: after proceed");
		return;
	}
	int around():set(int *.*)
	{
		System.out.println("illegal!");
		proceed();
		return 0;
	}
	double around(): get(double *.*)
	{
		System.out.println("double: before proceed");
		double result=proceed();
		System.out.println("double: after proceed");
		proceed();
		return result;
	}
	/*Object around(): get(Object *.*)
	{
		System.out.println("before proceed");
		Object result=proceed();
		System.out.println("after proceed");
		return result;
	}*/
}
