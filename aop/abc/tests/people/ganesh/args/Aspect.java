public aspect Aspect {

pointcut foo(Object a) : args(a,..);
pointcut bar(Object a) : args(..,a);

before(Object a,Object b) : !within(Aspect*) && foo(a) && bar(b) && args(..)
   {  
	System.out.println("foo: "+a+b);
   }
}
