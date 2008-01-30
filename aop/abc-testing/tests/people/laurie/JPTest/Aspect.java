public aspect Aspect {

  // This pair of advice decls demonstrates that an advice can change to
  //   the args of a join point.

  before () : call(* lottery(java.lang.String)) && !within(Aspect)
    { System.out.println("BEFORE " + thisJoinPoint +
	                 " at " + thisJoinPointStaticPart.getSourceLocation()); 
      Object args[] = thisJoinPoint.getArgs();
      System.out.println("arg[0] is " + args[0]);
      args[0] = "Laurie";
      System.out.println("... now it is " + thisJoinPoint.getArgs()[0]);
    }


  after ()  : call(* lottery(java.lang.String)) && !within(Aspect) 
    { System.out.println("AFTER " + thisJoinPoint +
	                 " at " + thisJoinPointStaticPart.getSourceLocation()); 
      if (thisJoinPoint.getArgs()[0].equals("Laurie"))
        System.out.println("Laurie wins 1 million pounds!");
    }

}
