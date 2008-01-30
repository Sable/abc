//Listing 6.6 ViolationBean.java: a bean that violates the EJB rules

package customer;

import javax.ejb.*;
import javax.naming.*;

public abstract class ViolationBean implements EntityBean {
    private static int _subscriptionCount = 0;
    // ...
    public void addSubscription (String subscriptionKey) {
	try {
	    Context ic = new InitialContext();
	    // ...
	} catch (Exception ex) {
	    javax.swing.JOptionPane.showMessageDialog(null,
				      "Exception while adding subscription");

	    ex.printStackTrace();
	}
	_subscriptionCount++;
    }
    // ...
}
