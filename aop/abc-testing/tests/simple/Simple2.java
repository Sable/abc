public aspect Simple2 percflow(adviceexecution()) { 

  /* these make sense to ajc 
  pointcut target(int x): args(x) && target(String);

  pointcut adviceexecution(): target(Object);

  declare warning: adviceexecution(): "what got applied?";

  declare warning: args(int): "what about this?";

  */
  // pointcut foo(int x): args(x) && target(x); 
}
