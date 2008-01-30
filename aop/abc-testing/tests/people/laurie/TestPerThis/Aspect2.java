public aspect Aspect2 perthis(call (* *..f(..)) 
                 /* && cflow(execution(* *..main(..))) */ ) {

static int x = 3;

// perthis clause overlaps with advice 1
//    calls to method m are not in the perthis,  while calls with
//    Aspect2 are not in the perthis.

  void message(String s) { System.out.println("****************" + s); }

  // advice 1
  before () : call(* *..*(..)) && !within(Aspect*) 
  {
    message("Advice 1: before calling " +
             thisJoinPointStaticPart.getKind()	+ " at " + 
	     thisJoinPointStaticPart.getSourceLocation() );

  }

  // advice 2
  before () : call(* *..m(..)) && !within(Aspect*) 
  {
    message("Advice 2: before calling " + 
	     thisJoinPointStaticPart.getKind() + " at " + 
	     thisJoinPointStaticPart.getSourceLocation() );

  }

  // advice 3
  before () : if(x==3) && cflowbelow(execution(* *..main(..))) 
              && call(* *..*(..))
              && !within(Aspect*) 
  {
    message("Advice 3: before calling " + 
	   thisJoinPointStaticPart.getKind() + " at " +
	     thisJoinPointStaticPart.getSourceLocation() );
  }

}

