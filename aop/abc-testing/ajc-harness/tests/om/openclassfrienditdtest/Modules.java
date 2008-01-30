module M1 {
	friend X;
	class Main;

	open M2;
	constrain M3;
}
module M2 {
	friend pack.Y;

	class A;
	class D;
}
module M3 {
	friend Z;

	class B;
	class E;
}
