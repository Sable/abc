public aspect Aspect {

pointcut foo(Object a) : args(a,..);

before(Object a) : !within(Aspect*) && foo(a) 
   {  
	System.out.println("arg0: "+a);
   }
}
