
public aspect Aspect {

    // This matches! How? 
    // Semantics: Objec* is the set of all types matching this pattern.
    // Implementation: Match against all supertypes of Integer to get rid of *.
    before():execution(Objec*+ *.baz(..)) {}	
    Integer baz() { return null; }
    
    /*
    before():call(* Fo*o*oo.foo(..)) {}

    before():call(* (Fo*o*oo || G*oo+ && asd..sdf..we.e..ds.Boo).foo(..)) {}

    before():call(* fo*o..sd*oo.Foo.foo(..)) {}

    before(): call((Foo || Goo) ((Boo || Baz) && Gaz).m()) {}
    before():call(Foo (Boo && Gaz).m()) || call(Foo (Baz && Gaz).m()) || call(Goo (Boo && Gaz).m()) || call(Goo (Baz && Gaz).m()) {}
    
    before(): within(!(Foo || Goo)) {}
    before(): within(!Foo && !Goo) {}
    before(): within(!Foo) && within(!Goo) {}
    */
	static class Foo { 
	    static class Boo { 
		static class Goo {

		}
	    }
	}

	before(): within(Aspect..Foo..Boo..Goo) {}

}