import java.util.*;

public aspect CheckReturnTypesCorrect {
	
	static void correct() {
		Set s = exhibit JPHashSet {
			return new HashSet(); //ok
		};
		Set s2 = exhibit JPSet {
			return new HashSet(); //ok
		};
		HashSet s3 = exhibit JPHashSet {
			return new HashSet(); //ok
		};
	}

	public static void main(String args[]) {
		correct();
	}

	jpi Set JPSet();

	jpi HashSet JPHashSet();
	
	HashSet around JPHashSet() {
		return new HashSet();
	}
	
	Set around JPSet() {
		return new TreeSet();
	}
}