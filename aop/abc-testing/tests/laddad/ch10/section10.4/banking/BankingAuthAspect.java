//Listing 10.10 BankingAuthAspect.java: authenticating banking operations

package banking;

import auth.AbstractAuthAspect;

public aspect BankingAuthAspect extends AbstractAuthAspect {
    public pointcut authOperations()
	: execution(public * banking.Account.*(..))
	|| execution(public * banking.InterAccountTransferSystem.*(..));
}
