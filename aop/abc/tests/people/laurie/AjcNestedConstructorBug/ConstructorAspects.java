public aspect ConstructorAspects {

  static private int aspectnesting = 0;

  static void message(String s)
    { for (int i=0; i<aspectnesting; i++) System.out.print("---+");
      System.out.println(s);
    }


  // call of all constructors 
  pointcut allconstrcalls() :  call(*..new(..)) &&
           !within(ConstructorAspects) && !call(java.lang..new(..));

  // execution of all constructors
  pointcut allconstrexecutions() : execution(*..new(..)) && 
           !within(ConstructorAspects);

  // intialization of all constructors
  pointcut allconstrinitializations() : initialization(*..new(..)) &&
           !within(ConstructorAspects);

  // preinitialization of all constructors
  pointcut allconstrpreinitializations() : preinitialization(*..new(..)) &&
          !within(ConstructorAspects);

  // before advice
  before () : !within(ConstructorAspects) {
	      message(
		  "BEFORE: " +  thisJoinPointStaticPart.getSourceLocation() + 
		  " " +thisJoinPointStaticPart.toLongString()); 
	      aspectnesting++;
	      }

  // after advice
  after () returning : !within(ConstructorAspects) {
              aspectnesting--;
	      message(
		  "AFTER: " +  thisJoinPointStaticPart.getSourceLocation() + 
		  " " +thisJoinPointStaticPart.toLongString()); 
	      }

}
