//Listing 10.14 AuthLogging.java: adding authorization logging

package banking;

import org.aspectj.lang.*;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import logging.*;

public aspect AuthLogging extends IndentedLogging {
    declare precedence: AuthLogging, *;

    public pointcut accountActivities()
	: call(void Account.credit(..))
	|| call(void Account.debit(..))
	|| call(* Account.getBalance(..))
	|| call(void InterAccountTransferSystem.transfer(..));

    public pointcut authenticationActivities()
	: call(* LoginContext.login(..));

    public pointcut authorizationActivities()
	: call(* Subject.doAsPrivileged(..));

    public pointcut loggedOperations()
	: accountActivities()
	|| authenticationActivities()
	|| authorizationActivities();

    before() : loggedOperations() {
	Signature sig = thisJoinPointStaticPart.getSignature();
	System.out.println("<" + sig.getName() + ">");
    }
}
