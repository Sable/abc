//Listing 4.17 AssociationDemoAspect.java: with perthis() association

public aspect AssociationDemoAspect
    perthis(accountOperationExecution(Account)) {

    public AssociationDemoAspect() {
	System.out.println("Creating aspect instance");
    }

    pointcut accountOperationExecution(Account account)
	: (execution(* Account.credit(..))
	   || execution(* Account.debit(..)))
	&& this(account);

    before(Account account)
	: accountOperationExecution(account) {
	System.out.println("JoinPoint: " + thisJoinPointStaticPart
			   + "\n\taspect: " + this
			   + "\n\tobject: " + account);
    }
}
