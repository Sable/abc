public aspect ConstructorAspects {

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
	      System.out.println(
		  "BEFORE: " +  thisJoinPointStaticPart.getSourceLocation() + 
		  " " +thisJoinPointStaticPart.toLongString()); 
	      }

  // after advice
  after () returning : !within(ConstructorAspects) {
	      System.out.println(
		  "AFTER: " +  thisJoinPointStaticPart.getSourceLocation() + 
		  " " +thisJoinPointStaticPart.toLongString()); 
	      }

}
