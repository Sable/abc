jpi void JP(int a);

aspect C{
	
	final void around JP(int m){}
	
	void around JP(int l){}

	void around JP(int l){} //error -->line 3
	
	before JP(int m){}
	
	after JP(int a){}
	
	before JP(int n){} //error -->line 3
	
	after JP(int h){} //error -->line 3
	
	final after JP(int l){} //error -->line 3
	
	public static void main(String[] args){
		
	}
}