package p;
class B{

	public static void m(){
		A.F= null;
		new A()///A
		.F= null;
		new A().i()///A
		.F= null;
		new A().i().i()///A
		.F= null;
		A.F= null;
		A.F= null;
	}
}