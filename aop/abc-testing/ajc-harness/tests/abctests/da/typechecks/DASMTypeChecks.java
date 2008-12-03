aspect DATypeChecks {
	
	dependent after  a1(Object o) returning(Object p): call(* foo()) && target(o) {};
	dependent before a2(Object o): call(* bar()) && target(o) {};

	//ok
	dependency {
		a1, a2;
		initial	s1: a1 -> s2;
				s2: a1 -> s3;
        final 	s3;
	}
	
	//variables impose no dependency
	dependency {
		a1(x,y), a2(z);
		initial	s1: a1 -> s2;
				s2: a1 -> s3;
        final 	s3;
	}
	
	//wrong number of variables (too few)
	dependency {
		a1(o), a2;
		initial	s1: a1 -> s2;
				s2: a1 -> s3;
        final 	s3;
	}

	//wrong number of variables (too many)
	dependency {
		a1, a2(o,o);
		initial	s1: a1 -> s2;
				s2: a1 -> s3;
        final 	s3;
	}
	
//	//variables impose no dependency but * is used (no warning); does not work due to lexer limitation at the moment
//	dependency {
//		a1(*,*), a2(*);
//		initial	s1: a1 -> s2;
//				s2: a1 -> s3;
//        final 	s3;
//	}
	
	//no initial state
	dependency {
		a1, a2;
		final s1; //unreachable from any initial state
	}	

	//no final state
	dependency {
		a1, a2;
		initial s1;//cannot reach final
	}	

	//(final) state not reachable
	dependency {
		a1, a2;
		initial	s1: a1 -> s1;//cannot reach final
        final s2;
	}	

	//state names not unique
	dependency {
		a1, a2;
		initial	s1: a1 -> s1;
        final s1;
	}	

	//target state does not exist
	dependency {
		a1, a2;
		initial	s1: a1 -> DUMMY,
					a1 -> s2;
        final s2;
	}	

	//referenced symbol does not exist
	dependency {
		a1, a2;
		initial	s1: DUMMY -> s2;
        final s2;
	}	

	//cannot reach final state from s2
	dependency {
		a1, a2;
		initial	s1: a1 -> s2,
					a2 -> s3;
		s2;
        final s3;
	}	
}