aspect DATypeChecks {
	
	dependent after  a1(Object o) returning(Object p): call(* foo()) && target(o) {};
	dependent before a2(Object o): call(* bar()) && target(o) {};

	/*
	 * T1:
	 * Variable name "i" is orphan, i.e. only exists in one advice name, therefore
	 * no dataflow dependency is imposed. A warning should be given to use the wildcard instead.
	 */	
	dependency {
		strong a1(i,j);
		weak a2(j);		
	}
	
	/*
	 * T2:
	 * Same as T1 but this time a wildcard is given. No warning should be issued.
	 */	
	dependency {
		strong a1(*,j);
		weak a2(j);		
	}
	
	/*
	 * T3:
	 * Same as T1,T2 but this time without any variable names given. No warning should be issued.
	 */	
	dependency {
		strong a1;
		weak a2;		
	}
	
	/*
	 * T4:
	 * Variable o is given and implicitly exists on a1 ("a1" is the same as "a1(o,p)"). 
	 * No warning should be given.
	 */	
	dependency {
		strong a1;
		weak a2(o);		
	}

	/*
	 * T5:
	 * Same as T4 but this time p is orphan. A warning should be given.
	 */	
	dependency {
		strong a1(*,p);
		weak a2;		
	}
	
	/*
	 * T6:
	 * Too few parameters given to a1.
	 * An error should be issued.
	 */	
	dependency {
		strong a1(*);
		weak a2;		
	}

	/*
	 * T6:
	 * Too many parameters given to a2.
	 * An error should be issued.
	 */	
	dependency {
		strong a1;
		weak a2(*,*);		
	}
	
}