import org.aspectj.testing.Tester;
import java.io.*;

jpi void JP() throws FileNotFoundException;

/*public aspect A{
	
	public static void main(String[] args){
		try{
			exhibit JP{
				System.out.println("sososo");
			};
		}
		catch(FileNotFoundException e){
			System.out.println("sososo");
		}
	}
	
	before JP(){
		System.out.println("...");
	}
}*/

public aspect A{
	
	exhibits void JP() : call(* foo(..));
	
	public static void main(String[] args){
		try{
			foo();
		}
		catch (FileNotFoundException f) {}
	}

	public static void foo() throws FileNotFoundException{}
	
	before JP(){
		System.out.println("me ejecute");
	}
	
}

