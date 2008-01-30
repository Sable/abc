public aspect Aspect {
    int around(Test x): call(int Test.foo(int)) && (this(x) || target(x)) {
	System.out.println("advice 0 before proceed");
	int ret=proceed(new Test2(-1));
	System.out.println("advice 0 after proceed");
	return ret;
    }

    int around(Test x,Test y): call(int foo(int)) && this(x) && target(y){
	System.out.println("advice 1 before proceed: "+display(x,y));
	System.out.println(thisJoinPoint.getThis()+","+thisJoinPoint.getTarget());
	int ret=proceed(y,x);
	System.out.println("advice 1 after proceed: "+display(x,y));
	return ret;
    }

    int around(Test x,Test y): call(int foo(int)) && this(x) && target(y){
	System.out.println("advice 2 before proceed: "+display(x,y));
	System.out.println(thisJoinPoint.getThis()+","+thisJoinPoint.getTarget());
	int ret=proceed(y,x);
	System.out.println("advice 2 after proceed: "+display(x,y));
	return ret;
    }

    int around(Object x): execution(int foo(float)) && args(x) {
	return proceed(x);
    }

    void around(Test x,Test y):execution(void bar()) && this(x) && target(y) {
	System.out.println("advice 3 before proceed: "+display(x,y));
	proceed(new Test2(0),new Test2(1));
	System.out.println("advice 3 after proceed: "+display(x,y));
    }

    void around(Test x,Test y):execution(void bar()) && this(x) && target(y) {
	System.out.println("advice 4 before proceed: "+display(x,y));
	proceed(x,y);
	System.out.println("advice 4 after proceed: "+display(x,y));
    }

    String display(Object x,Object y) {
	return "this="+x+" target="+y;
    }

}
