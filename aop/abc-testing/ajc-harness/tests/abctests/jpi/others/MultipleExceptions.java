
import java.io.*;

jpi void I(int a) throws ArrayIndexOutOfBoundsException, IOException;

class A{
	
	exhibits void I(int a) : call(void *(..)) && args(a);
	
	public static void foo(int t) throws ArrayIndexOutOfBoundsException{ throw new ArrayIndexOutOfBoundsException();}

	public static void bar(int t) throws IOException{ throw new IOException();}
	
	public static void main(String[] args){
		try{
			foo(1);
		} 
		catch (ArrayIndexOutOfBoundsException a){
			System.out.println(a.toString());
		}
		try{
			bar(1);
		} 
		catch (IOException b){
			System.out.println(b.toString());
		}
		
	}
}

aspect B{
	
	void around I(int a) throws ArrayIndexOutOfBoundsException, IOException{
		try{
			proceed(a);
		}
		catch (ArrayIndexOutOfBoundsException a){
			throw new IOException();
		}
		catch(IOException b){
			throw new IOException();
		}
	}
}