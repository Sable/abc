public aspect Aspect {

pointcut foo(Object a) : args(a,..);
pointcut bar(Object a) : args(..,a);

before(Object a,Object b) : !within(Aspect*) && foo(a) && bar(b) && args(..)
   {  
	System.out.println("foo: "+a+b);
   }

pointcut splat(int x) : args(x);

before(Object x): !within(Aspect*) && splat(x) {}
}
