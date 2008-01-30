//Listing 10.6 AuthLogging.java: logging banking operations

package banking;

import org.aspectj.lang.*;
import logging.*;

public aspect AuthLogging extends IndentedLogging {
    declare precedence: AuthLogging, *;
    
    public pointcut accountActivities()
	: execution(public * Account.*(..))
	|| execution(public * InterAccountTransferSystem.*(..));

    public pointcut loggedOperations()
	: accountActivities();

    before() : loggedOperations() {
	Signature sig = thisJoinPointStaticPart.getSignature();
	System.out.println("<" + sig.getName() + ">");
    }
}


