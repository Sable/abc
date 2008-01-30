//Listing 4.18 AssociationDemoAspect.java: with percflow() association

public aspect AssociationDemoAspect
    percflow(accountOperationExecution(Account)) {

    public AssociationDemoAspect() {
	System.out.println("Creating aspect instance");
    }

    pointcut accountOperationExecution(Account account)
	: (execution(* Account.credit(..))
	   || execution(* Account.debit(..)))
	&& this(account);

    before(Account account)
	: accountOperationExecution(account)
	|| (execution(* Account.setBalance(..)) && this(account)) {
	System.out.println("JoinPoint: " + thisJoinPointStaticPart
			   + "\n\taspect: " + this
			   + "\n\tobject: " + account);
    }
}
