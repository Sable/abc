//Listing 6.5 DetectEJBViolations.java: ensuring EJB policy enforcement

import javax.ejb.*;

public aspect DetectEJBViolations {
    pointcut uiCalls() : call(* java.awt..*+.*(..));

    declare error : uiCalls() && within(EnterpriseBean+)
	: "UI calls are not allowed from EJB beans.\n See EJB 2.0 specification section 24.1.2";
}
