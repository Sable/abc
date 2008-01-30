//Listing 12.14 An aspect that logs account activities

package banking;

import logging.*;

public aspect LogAccountActivities extends IndentedLogging {
    declare precedence : LogAccountActivities, *;

    pointcut accountActivity(Account account, float amount)
	: ((execution(void Account.credit(float))
	  || execution(void Account.debit(float)))
	  && this(account)
	  && args(amount))
	  || (execution(void CheckClearanceSystem.*(Account, float))
	  && args(account, amount));
    
    protected pointcut loggedOperations()
	: accountActivity(Account, float);

    void around(Account account, float amount)
	: accountActivity(account, amount) {
	try {
	    System.out.println("[" +
		       thisJoinPointStaticPart.getSignature().toShortString()
			       + "] " + account + " " + amount);
	    System.out.println("Before: " + account.getBalance());
	    proceed(account, amount);
	} finally {
	    System.out.println("After: " + account.getBalance());
	}
    }
}
