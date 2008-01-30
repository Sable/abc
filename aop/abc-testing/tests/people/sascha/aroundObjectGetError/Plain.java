
public aspect Plain {

	//int i;

	int around(): get(int *.*)
	{
		System.out.println("int: before proceed");
		int result=proceed();
		System.out.println("int: after proceed");
		return result;
	}

	double around(): get(double *.*)
	{
		System.out.println("double: before proceed");
		double result=proceed();
		System.out.println("double: after proceed");
		return result;
	}
	Object around(): get(Object *.*)
	{
		System.out.println("before proceed");
		Object result=proceed();
		System.out.println("after proceed");
		return result;
	}
}
