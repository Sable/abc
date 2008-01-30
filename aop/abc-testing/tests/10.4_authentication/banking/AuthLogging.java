//Listing 10.8 AuthLogging.java: with authentication logging implemented

package banking;

import org.aspectj.lang.*;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;

import logging.*;

public aspect AuthLogging extends IndentedLogging {
    declare precedence: AuthLogging, *;

    public pointcut accountActivities()
	: execution(public * Account.*(..))
	|| execution(public * InterAccountTransferSystem.*(..));

    public pointcut authenticationActivities()
	: call(* LoginContext.login(..));

    public pointcut loggedOperations()
	: accountActivities()
	|| authenticationActivities();

    before() : loggedOperations() {
	Signature sig = thisJoinPointStaticPart.getSignature();
	System.out.println("<" + sig.getName() + ">");
    }
}
