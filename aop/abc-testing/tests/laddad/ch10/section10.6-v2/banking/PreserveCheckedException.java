//Listing 10.18 PreserveCheckedException.java: Aspect preserving checked exceptions

package banking;

import auth.AbstractAuthAspect;

public aspect PreserveCheckedException {
    after() throwing(AbstractAuthAspect.AuthorizationException ex)
	throws InsufficientBalanceException
	: call(* banking..*.*(..)
	       throws InsufficientBalanceException) {
	Throwable cause = ex.getCause();
	if (cause instanceof InsufficientBalanceException) {
	    throw (InsufficientBalanceException)cause;
	}
	throw ex;
    }
}
