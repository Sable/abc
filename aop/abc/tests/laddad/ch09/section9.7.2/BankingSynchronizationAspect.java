//Listing 9.17 BankingSynchronizationAspect.java

aspect BankingSynchronizationAspect
    extends ReadWriteLockSynchronizationAspect {
    public pointcut readOperations()
	: execution(* Account.get*(..))
	|| execution(* Account.toString(..));

    public pointcut writeOperations()
	: execution(* Account.*(..))
	&& !readOperations();
}
