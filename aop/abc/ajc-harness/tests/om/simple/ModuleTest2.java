module ModuleTest2 {
	friend AspectB;
	open ModuleTest3;
	expose : call(* *(..));
}
