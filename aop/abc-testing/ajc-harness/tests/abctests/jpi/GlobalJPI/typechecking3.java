import java.lang.*;

global jpi String JP() : call(String myfoo(..));
jpi Float JP1();

class A{
	exhibits String JP() : execution(String mybar()) || global();  //ok
	exhibits Float JP1() : execution(Float bar(..)) || global(); //error
	
	
	public static String mybar(){return null;}
	public static Float bar(){return null;}
}

public class C{
	
	public static String myfoo(){return null;}		
	public static void main(String[] args){
		A.mybar();
		myfoo(); 
	}
}
