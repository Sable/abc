import java.util.*;

public aspect CheckArgumentTypes {
	
	static void invariant(Set s) {
		exhibit JPSet(Set l){}(s);		
	}

	static void contravariant(Set s) {
		exhibit JPSet(Object o){}(s);		//error: declared type must be Set, not Object
	}

	static void contravariantList(List l) {
		exhibit JPSet(Object o){}(l);		//error: declared type must be Set, not Object		
	}

	public static void main(String args[]) {
		Set s = new HashSet();
		invariant(s);
		contravariant(s);
		List l = new LinkedList();
		contravariantList(l);
	}
	
	joinpoint void JPSet(Set s);
	
	before JPSet(Set s) {
		System.out.println(s.getClass()); //this would fail, would we allow contravariantList to execute, as a list would be passed as a parameter
	}
	
	
}