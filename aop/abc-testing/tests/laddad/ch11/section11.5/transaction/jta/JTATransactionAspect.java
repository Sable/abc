//Listing 11.13 JTATransactionAspect.java: the base aspect

package transaction.jta;

import javax.naming.*;
import javax.transaction.*;
import pattern.worker.*;

public abstract aspect JTATransactionAspect {
    protected abstract pointcut transactedOperation();

    protected pointcut
	inTransactedOperation(TransactionContext context)
	: cflow(execution(* TransactionContext.run())
		&& this(context));

    Object around() : transactedOperation()
	&& !inTransactedOperation(TransactionContext) {
	TransactionContext transactionContext
	    = new TransactionContext() {
		    public void run() {
			UserTransaction ut = null;
			try {
			    Context ctx = new InitialContext();
			    ut = (UserTransaction)
				ctx.lookup("java:comp/ut");
			} catch (NamingException ex) {
			    throw new TransactionException(ex);
			}
			try {
			    ut.begin();
			    _returnValue = proceed();
			    ut.commit();
			} catch (Exception ex) {
			    ut.rollback();
			    throw new TransactionException(ex);
			}
		    }};
	transactionContext.run();
	return transactionContext.getReturnValue();
    }

    public static abstract class TransactionContext
	extends RunnableWithReturn {
    }

    public static class TransactionException
	extends RuntimeException {
	public TransactionException(Exception cause) {
	    super(cause);
	}
    }

    private static aspect SoftenSystemException {
	declare soft : javax.transaction.SystemException
	    : call(void UserTransaction.rollback())
	    && within(JTATransactionAspect);
    }
}
