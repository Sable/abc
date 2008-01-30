//Listing 10.16 AbstractAuthAspect.java: adding authorization capabilities

package auth;

import org.aspectj.lang.JoinPoint;
import java.security.*;
import javax.security.auth.Subject;
import javax.security.auth.login.*;
import com.sun.security.auth.callback.TextCallbackHandler;

public abstract aspect AbstractAuthAspect {
    private Subject _authenticatedSubject;
    public abstract pointcut authOperations();

    before() : authOperations() {
	if(_authenticatedSubject != null) {
	    return;
	}
	try {
	    authenticate();
	} catch (LoginException ex) {
	    throw new AuthenticationException(ex);
	}
    }

    public abstract Permission getPermission(
			     JoinPoint.StaticPart joinPointStaticPart);

    Object around()
	: authOperations() && !cflowbelow(authOperations()) {
	try {
	    return Subject
		.doAsPrivileged(_authenticatedSubject,
				new PrivilegedExceptionAction() {
					public Object run() throws Exception {
					    return proceed();
					}}, null);
	} catch (PrivilegedActionException ex) {
	    throw new AuthorizationException(ex.getException());
	}
    }

    before() : authOperations() {
	AccessController.checkPermission(
			 getPermission(thisJoinPointStaticPart));
    }

    private void authenticate() throws LoginException {
	LoginContext lc = new LoginContext("Sample",
					   new TextCallbackHandler());
	lc.login();
	_authenticatedSubject = lc.getSubject();
    }

    public static class AuthenticationException
	extends RuntimeException {
	public AuthenticationException(Exception cause) {
	    super(cause);
	}
    }
    
    public static class AuthorizationException
	extends RuntimeException {
	public AuthorizationException(Exception cause) {
	    super(cause);
	}
    }
}
