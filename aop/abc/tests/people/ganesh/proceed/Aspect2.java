public aspect Aspect2 {
    
    //    before(Test2 x):call(int Test.foo(int)) && (this(x) || target(x)) {
    //    }

    
    int around(Test2 x): call(int Test.foo(int)) && (this(x) || target(x)) {
	System.out.println("advice 1 before proceed");
	int ret=proceed(new Test2(-1));
	System.out.println("advice 1 after proceed");
	return ret;
    }
    

}
