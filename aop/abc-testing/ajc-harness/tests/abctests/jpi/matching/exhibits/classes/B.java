package classes.modificables;

import interfaces.*;
import org.aspectj.testing.Tester;

public class B{
	
	exhibits void CallFoo(): call(* foo());
	
	public void foo(){
		Tester.check(false,"should not execute");
	}
	
	public void bar(){
		foo();
	}
}