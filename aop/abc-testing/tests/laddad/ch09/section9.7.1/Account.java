//Listing 9.15 Account.java: with the read-write lock pattern implemented

import EDU.oswego.cs.dl.util.concurrent.*;

public abstract class Account {
    private float _balance;
    private int _accountNumber;
    
    private ReadWriteLock _lock
	= new ReentrantWriterPreferenceReadWriteLock();

    public Account(int accountNumber) {
	_accountNumber = accountNumber;
    }

    public void credit(float amount) {
	try {
	    _lock.writeLock().acquire();
	    setBalance(getBalance() + amount);
	} catch (InterruptedException ex) {
	    throw new RuntimeException(ex);
	} finally {
	    _lock.writeLock().release();
	}
    }

    public void debit(float amount)
	throws InsufficientBalanceException {
	try {
	    _lock.writeLock().acquire();
	    float balance = getBalance();
	    if (balance < amount) {
		throw new InsufficientBalanceException(
			       "Total balance not sufficient");
	    } else {
		setBalance(balance - amount);
	    }
	} catch (InterruptedException ex) {
	    throw new RuntimeException(ex);
	} finally {
	    _lock.writeLock().release();
	}
    }

    public float getBalance() {
	try {
	    _lock.readLock().acquire();
	    return _balance;
	} catch (InterruptedException ex) {
	    throw new RuntimeException(ex);
	} finally {
	    _lock.readLock().release();
	}
    }

    public void setBalance(float balance) {
	try {
	    _lock.writeLock().acquire();
	    _balance = balance;
	} catch (InterruptedException ex) {
	    throw new RuntimeException(ex);
	} finally {
	    _lock.writeLock().release();
	}
    }
}
