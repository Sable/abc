//Listing 10.9 AbstractAuthAspect.java: the base authentication aspect

package auth;

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
}
