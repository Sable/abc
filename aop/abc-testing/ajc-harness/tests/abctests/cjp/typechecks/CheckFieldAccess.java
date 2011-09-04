public aspect CheckFieldAccess {
	
	int i;
	final int j=0;
	
	void fieldAccess ()  {
		exhibit JP { i=1; };	// no error
	}
	
	void fieldAccess ()  {
		exhibit JP { int i=j; };	//no error
	}
	
	void fieldAccess ()  {
		exhibit JP { j=1; }; //error: assignment to final field 
	}

	jpi void JP();
}