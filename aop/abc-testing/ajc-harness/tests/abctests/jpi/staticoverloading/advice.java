import java.lang.*;

jpi int JP1(float c);
jpi void JP2();
jpi void JP1(int b) extends JP2();
jpi float JP1(boolean d) throws Exception;
jpi Integer JP1(Integer a);

aspect A{
	
	void around JP1(char l){ //error, there is no such JP1 definition.
		proceed(l);
	}
	
	void around JP1(float c){} //error
	
	int around JP1(float a){return 1;} //ok
	
	void around JP2(boolean m){} //error
	
	void around JP2(){} //ok	
	
	Integer around JP1(Integer l){ return new Integer(12);}
}

