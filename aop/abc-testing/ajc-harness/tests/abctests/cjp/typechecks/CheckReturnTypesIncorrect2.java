import java.util.*;

public aspect CheckReturnTypesIncorrect2 {
	
	static void correct() {
		Set s = exhibit JPHashSet {  //error: cannot apply around advice returning Set here //TODO: this should better give an error on the advice declaration 
			return new HashSet();
		};
	}

	public static void main(String args[]) {
		correct();
	}

	joinpoint HashSet JPHashSet();
	
	Set around JPHashSet() {			
		return new TreeSet();
	}
	
}