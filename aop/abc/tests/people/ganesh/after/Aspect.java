public aspect Aspect {
   after() throwing(RuntimeException e) : within(Test) { // call(* foo(int)) {
	System.out.println(e);
        e=new RuntimeException("foo");
   }

    after() returning(Object e) : within(Test) { // execution(* foo(*)) && args(Object) {
	System.out.println(e.toString());
    }
}
