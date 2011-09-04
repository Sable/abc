import java.util.*;

public aspect CheckReturnTypesIncorrect {
	
	static void wrongActualReturnType() {
		Set s = exhibit JPHashSet {
			return new Object(); //Error: Object is no HashSet
		};
	}

	static void wrongDeclaredReturnType() {
		HashSet s = exhibit JPSet {  //Error: Set is no HashSet
			return new HashSet();
		};
	}

	public static void main(String args[]) {
		wrongActualReturnType();
		wrongDeclaredReturnType();
	}

	jpi Set JPSet();

	jpi HashSet JPHashSet();
	
}