package aspects;

import interfaces.*;

aspect A{
	
	exhibits void Root(int a): call(* *(..)) && args(a);

	void around Root(int a){
		
	}

}

