//Listing 10.5 Test.java: version with no authentication or authorization

package banking;

public class Test {
    public static void main(String[] args) throws Exception {
	Account account1 = new AccountSimpleImpl(1);
	Account account2 = new AccountSimpleImpl(2);
	account1.credit(300);
	account1.debit(200);
	
	InterAccountTransferSystem.transfer(account1, account2, 100);
	InterAccountTransferSystem.transfer(account1, account2, 100);
    }
}
