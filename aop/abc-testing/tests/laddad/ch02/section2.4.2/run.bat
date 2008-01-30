del *.class

call ajc Account.java SavingsAccount.java InsufficientBalanceException.java JoinPointTraceAspect.java Test.java
@echo on
call java Test
