//Listing 6.5 DetectEJBViolations.java: ensuring EJB policy enforcement

import javax.ejb.*;

public aspect DetectEJBViolations {
    pointcut staticMemberAccess() :
	set(static * EnterpriseBean+.*);

    declare error : staticMemberAccess()
	: "EJBs are not allowed to have nonfinal static variables.\n See EJB 2.0 specification section 24.1.2";
}
