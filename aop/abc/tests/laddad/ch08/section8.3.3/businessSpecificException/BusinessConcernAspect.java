//Listing 8.15 BusinessConcernAspect.java

public aspect BusinessConcernAspect extends ConcernAspect {
    pointcut operations() : call(* BusinessClass.business*());
}
