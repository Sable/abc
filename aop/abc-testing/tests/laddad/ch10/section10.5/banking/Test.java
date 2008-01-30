//Listing 10.13 Test.java: the conventional way

package banking;

import java.security.*;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import com.sun.security.auth.callback.TextCallbackHandler;

public class Test {
    public static void main(String[] args) throws Exception {
	LoginContext lc
	    = new LoginContext("Sample",
			       new TextCallbackHandler());
	lc.login();
	final Account account1 = new AccountSimpleImpl(1);
	final Account account2 = new AccountSimpleImpl(2);
	Subject authenticatedSubject = lc.getSubject();
	Subject
	    .doAsPrivileged(authenticatedSubject,
			    new PrivilegedAction() {
				    public Object run() {
					account1.credit(300);
					return null;
				    }}, null);
	try {
	    Subject
		.doAsPrivileged(authenticatedSubject,
				new PrivilegedExceptionAction() {
					public Object run() throws Exception {
					    account1.debit(200);
					    return null;
					}}, null);
	} catch (PrivilegedActionException ex) {
	    Throwable cause = ex.getCause();
	    if (cause instanceof InsufficientBalanceException) {
		throw (InsufficientBalanceException)ex.getCause();
	    }
	}

	try {
	    Subject
		.doAsPrivileged(authenticatedSubject,
				new PrivilegedExceptionAction() {
					public Object run() throws Exception {
					    InterAccountTransferSystem
						.transfer(account1, account2,
							  100);
					    return null;
					}}, null);
	} catch (PrivilegedActionException ex) {
	    Throwable cause = ex.getCause();
	    if (cause instanceof InsufficientBalanceException) {
		throw (InsufficientBalanceException)ex.getCause();
	    }
	}

	try {
	    Subject
		.doAsPrivileged(authenticatedSubject,
				new PrivilegedExceptionAction() {
					public Object run() throws Exception {
					    InterAccountTransferSystem
						.transfer(account1, account2,
							  100);
					    return null;
					}}, null);
	} catch (PrivilegedActionException ex) {
	    Throwable cause = ex.getCause();
	    if (cause instanceof InsufficientBalanceException) {
		throw (InsufficientBalanceException)ex.getCause();
	    }
	}
    }
}
