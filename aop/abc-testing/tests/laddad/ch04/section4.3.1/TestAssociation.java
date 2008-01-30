//Listing 4.16 TestAssociation.java: testing associations

public class TestAssociation {
    public static void main(String[] args) throws Exception {
	SavingsAccount account1 = new SavingsAccount(12245);
	SavingsAccount account2 = new SavingsAccount(67890);
	account1.credit(100);
	account1.debit(100);
	account2.credit(100);
	account2.debit(100);
    }
}
