module M1 {
	openclass parent(A) : A;
	open M2;
}
module M2 {
	openclass field : B;
	constrain M3;
}
module M3 {
	openclass method : C;
	open M4;
}
module M4 {
	openclass : C;
}
