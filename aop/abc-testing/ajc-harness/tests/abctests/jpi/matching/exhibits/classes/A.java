package classes.modificables;

import interfaces.*;
import org.aspectj.testing.Tester;

public class A{
	
	exhibits void CallFoo(): call(* foo());
	
	public void foo(){
		Tester.check(false,"should not execute");		
	}
	
	public void bar(){
		//foo();
		B b = new B();
		b.bar();
	}
	
	public static void main(String[] args){
		A a = new A();
		a.bar();
	}
}