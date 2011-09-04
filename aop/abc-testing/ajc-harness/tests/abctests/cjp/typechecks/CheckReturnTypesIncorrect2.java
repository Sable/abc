import java.util.*;

public aspect CheckReturnTypesIncorrect2 {
	
	static void correct() {
		HashSet s = exhibit JPHashSet {  
			return new HashSet();
		};
	}

	public static void main(String args[]) {
		correct();
	}

	jpi HashSet JPHashSet();
	
	Set around JPHashSet() {	//error: return type must be HashSet
		return new TreeSet();
	}
	
}