//Listing 6.5 DetectEJBViolations.java: ensuring EJB policy enforcement

import javax.ejb.*;

public aspect DetectEJBViolations {
    pointcut uiCalls() : call(* java.awt.*+.*(..));

    declare error : uiCalls() && within(EnterpriseBean+)
	: "UI calls are not allowed from EJB beans.\n See EJB 2.0 specification section 24.1.2";
    
    before() : uiCalls() && cflow(call(* EnterpriseBean+.*(..))) {
	System.out.println("Detected call to AWT from enterprise bean");
	System.out.println("See EJB 2.0 specification section 24.1.2");
	Thread.dumpStack();
    }

    // Implementation of other programming restrictions:
    // Socket, file i/o, native library loading, keyboard input
    // thread methods access, reflection etc.
    pointcut staticMemberAccess() :
	set(static * EnterpriseBean+.*);

    declare error : staticMemberAccess()
	: "EJBs are not allowed to have nonfinal static variables.\n See EJB 2.0 specification section 24.1.2";
}
