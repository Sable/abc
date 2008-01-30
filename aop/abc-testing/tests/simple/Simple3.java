public aspect Simple3  { 
   pointcut foo() : within(aspect.get.privileged);

   pointcut goo() : get(* aspect.set.get.f);

}
