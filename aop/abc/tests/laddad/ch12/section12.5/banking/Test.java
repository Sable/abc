//Listing 12.13 The test program

package banking;

import java.io.*;

public class Test {
    public static void main(String[] args) throws Exception {
	Customer customer1 = new Customer("Customer1");
	Account savingsAccount
	    = new SavingsAccountSimpleImpl(1, customer1);
	Account checkingAccount
	    = new CheckingAccountSimpleImpl(2, customer1);
	customer1.addAccount(savingsAccount);
	customer1.addAccount(checkingAccount);
	customer1.addOverdraftAccount(savingsAccount);
	savingsAccount.credit(1000);
	checkingAccount.credit(1000);
	savingsAccount.debit(500);
	savingsAccount.debit(480);
	
	checkingAccount.debit(500);
	checkingAccount.debit(480);
	checkingAccount.debit(100);
	CheckClearanceSystem.debit(checkingAccount, 400);
	CheckClearanceSystem.debit(checkingAccount, 600);
    }
}

aspect LogInsufficientBalanceException {
    pointcut methodCall() : call(void *.debit(..))
	&& within(Test);

    void around() : methodCall() {
	try {
	    proceed();
	} catch(InsufficientBalanceException ex) {
	    System.out.println(ex);
	}
    }
}
