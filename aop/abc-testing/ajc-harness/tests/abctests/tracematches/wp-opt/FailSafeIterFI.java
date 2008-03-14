import java.util.*;

public aspect FailSafeIterFI {

    pointcut collection_update(Collection c) :
     (  call(* java.util.Collection+.add*(..)) ||
        call(* java.util.Collection+.clear()) ||
        call(* java.util.Collection+.remove*(..)) ) && target(c);

     tracematch(Collection c, Iterator i) {
    	sym create_iter after returning(i) :( call(* java.util.Collection+.iterator()) && target(c)) && !within(dacapo..*);
    	sym call_next before:( call(* java.util.Iterator+.next()) && target(i)) && !within(dacapo..*);
    	sym update_source before :(  collection_update(c)) && !within(dacapo..*);

    	create_iter call_next* update_source+ call_next
    	{
    	}
    }

     /*
      * This used to trip the matrix based flow-insensitive analysis, for the following reason:
      * 
      * Altough both methods main and foo() use completely distinct collections and iterators,
      * the shadows 1 and 2 (see below) overlap, because they have no contradicting binding.
      * 
      * This then made the flow-sensitive analysis believe that the call to foo() may call
      * shadows that overlap with shadows in main!
      *
      * The new version of the matrix based flow-insensitive analysis now recognizes that
      * there is no complete set of strong shadows binding both the values at 1 and 2.
      * Therefore shadows 1 and 2 are assumed not to overlap, and the flow-*sen*sitive stage can remove all shadows.
      */

     public static void main(String[] args) {
    	
    	Collection c = new HashSet();
		foo();
    	c.add("");
    	for (Iterator iterator = c.iterator(); iterator.hasNext();) {
			iterator.next();	//1		
		}
    }
    
    public static void foo() {
    	Collection c = new HashSet();
    	c.add("");//2
    	for (Iterator iterator = c.iterator(); iterator.hasNext();) {
			iterator.next();			
		}
    }

     
}
   
