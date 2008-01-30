(deftemplate account (slot availBalance) (slot type))
(defrule account-existance
	(test (neq (fetch current-account) nil))
	=>
	(bind ?account-object (fetch current-account))
	(bind ?account-avail (call ?account-object getAvailableBalance))
	(if (instanceof ?account-object banking.SavingsAccount) then
		(bind ?account-type savings)
	else (if (instanceof ?account-object banking.CheckingAccount) then
		(bind ?account-type checking)))
	(assert (account (type ?account-type)
		(availBalance ?account-avail)))
	(assert (transaction-amount (fetch transaction-amount)))
	(assert (isCheckClearance (fetch checkClearanceTransaction)))
)
(defrule minimum-balance
	(account (availBalance ?account-avail) (type savings))
	(transaction-amount ?amount)
	(test (< ?account-avail ?amount))
	=>
	(throw (new banking.InsufficientBalanceException
	    "Minimum balance condition not met"))
)
(defrule overdraft-protection
	(account (availBalance ?account-avail) (type checking))
	(transaction-amount ?amount)
	(isCheckClearance TRUE)
	(test (< ?account-avail ?amount))
	=>
	(bind ?account-object (fetch current-account))
	(bind ?customer (call ?account-object getCustomer))
	(bind $?overdraft-accounts
		(call (call ?customer getOverdraftAccounts) toArray))
	(bind ?transfer-amount (- ?amount ?account-avail))
	(foreach ?overdraft-account $?overdraft-accounts
		(bind ?overdraft-avail
		(call ?overdraft-account getAvailableBalance))
		(if (< ?transfer-amount ?overdraft-avail) then
			(call ?overdraft-account debit ?transfer-amount)
			(call ?account-object credit ?transfer-amount)
			(return)
		)
	)
	(throw (new banking.InsufficientBalanceException
	"Insufficient funds in overdraft accounts"))
)

