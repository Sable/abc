package aspects;

import interfaces.*;

public aspect M{
	
	void around CallFoo(){
	//	//do nothing;
	}
	
	//void around(): ((call(* foo(..))) && (within(classes.A))) || ((call(* foo(..))) && (within(classes.B))){
	
	//}
}