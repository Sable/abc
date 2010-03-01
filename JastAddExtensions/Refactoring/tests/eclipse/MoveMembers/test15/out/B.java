package p;
class B{

	public static void m(){
		A.F= 0;
		new A()///A
		.F= 0;
		new A().i()///A
		.F= 0;
		new A().i().i()///A
		.F= 0;
	}
}