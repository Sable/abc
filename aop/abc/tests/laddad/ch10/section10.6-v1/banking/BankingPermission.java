//Listing 10.11 BankingPermission.java: permission class for banking system authorization

package banking;

import java.security.*;

public final class BankingPermission extends BasicPermission {
    public BankingPermission(String name) {
	super(name);
    }

    public BankingPermission(String name, String actions) {
	super(name, actions);
    }
}
