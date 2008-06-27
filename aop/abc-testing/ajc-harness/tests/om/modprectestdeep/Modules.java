module MA {
	open MB;
	friend A;
	open MC;
}
module MG {
	open MJ;
	friend G;
	open MK;
}
module MD {
	friend D;
}
module ME {
	friend E;
}
module MC {
	open MF;
	friend C;
	open MG;
}
module MF {
	open MH;
	friend F;
	open MI;
}
module MB {
	open MD;
	friend B;
	open ME;
}
module MH {
	friend H;
}
module MI {
	friend I;
}
module MJ {
	friend J;
}
module MK {
	friend K;
}
