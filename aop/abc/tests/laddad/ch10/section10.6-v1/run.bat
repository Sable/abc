del banking\*.class
del logging\*.class
del sample\module\*.class
del sample\principal\*.class

call ajc -d . banking\*.java auth\*.java logging\*.java sample\module\*.java sample\principal\*.java
@echo on

call java -Djava.security.auth.login.config=sample_jaas.config -Djava.security.policy=security.policy banking.Test