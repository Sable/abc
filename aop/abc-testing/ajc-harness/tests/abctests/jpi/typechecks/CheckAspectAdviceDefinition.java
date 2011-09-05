jpi void JP(int a);

aspect C{
	
	final void around JP(int m){}
	
	void around JP(int l){}

	void around JP(int l){}
	
	before JP(int m){}
	
	after JP(int a){}
	
	before JP(int n){}
	
	after JP(int h){} 
	
	final after JP(int l){} //error must occur before non-final pieces of advice
	
	public static void main(String[] args){
		
	}
}