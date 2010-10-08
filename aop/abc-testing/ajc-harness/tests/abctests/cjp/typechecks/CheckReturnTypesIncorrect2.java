import java.util.*;

public aspect CheckReturnTypesIncorrect2 {
	
	static void correct() {
		Set s = exhibit JPHashSet {  
			return new HashSet();
		};
	}

	public static void main(String args[]) {
		correct();
	}

	joinpoint HashSet JPHashSet();
	
	Set around JPHashSet() {	//error: return type must be HashSet
		return new TreeSet();
	}
	
}