jpi void JP1(int b);
jpi int JP1(float c);
jpi float JP1(boolean d) throws Exception;

aspect a{
	exhibits float JP1(boolean m) : execution(float *(..)) && argsinv(m); 
	
	exhibits void JP1(int k) : execution(* foo(..)) && argsinv(k);
	
	public int foo(int n){return 1;} //error->return type
	
	public float arf(boolean l){ return 1.5f;} //error->exceptions
	
}
