//Listing 11.3 Test.java: a test scenario for the transaction integrity problem

package banking;

public class Test {
    public static void main(String[] args) throws Exception {
	Account account1 = new AccountJDBCImpl(1);
	Account account2 = new AccountJDBCImpl(2);
	account1.credit(300);
	account1.debit(200);
	InterAccountTransferSystem.transfer(account1, account2, 100);
	InterAccountTransferSystem.transfer(account1, account2, 100);
    }
}

