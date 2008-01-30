//Listing 10.17 BankingAuthAspect.java: adding authorization capabilities

package banking;

import org.aspectj.lang.JoinPoint;
import java.security.Permission;
import auth.AbstractAuthAspect;

public aspect BankingAuthAspect extends AbstractAuthAspect {
    public pointcut authOperations()
	: execution(public * banking.Account.*(..))
	|| execution(public * banking.InterAccountTransferSystem.*(..));

    public Permission getPermission(
		   JoinPoint.StaticPart joinPointStaticPart) {
	return new BankingPermission(
		   joinPointStaticPart.getSignature().getName());
    }
}
