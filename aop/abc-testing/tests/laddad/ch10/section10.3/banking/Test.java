//Listing 10.7 Test.java: with authentication functionality

package banking;

import javax.security.auth.login.LoginContext;
import com.sun.security.auth.callback.TextCallbackHandler;

public class Test {
    public static void main(String[] args) throws Exception {
	LoginContext lc
	    = new LoginContext("Sample",
			       new TextCallbackHandler());
	lc.login();
	Account account1 = new AccountSimpleImpl(1);
	Account account2 = new AccountSimpleImpl(2);
	account1.credit(300);
	account1.debit(200);
	InterAccountTransferSystem.transfer(account1, account2, 100);
	InterAccountTransferSystem.transfer(account1, account2, 100);
    }
}
