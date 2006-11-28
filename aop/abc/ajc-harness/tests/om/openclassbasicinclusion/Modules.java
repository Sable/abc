module M1 {
	class Main;
	open M2;
	constrain M3;
	openclass parent : *;
}

module M2 {
	class A;
	openclass method : *;
}

module M3 {
	class B;
	openclass : *;
}
