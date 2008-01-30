//Listing 8.31 SystemErrorReportingAspect.java

public aspect SystemErrorReportingAspect
    extends AbstractErrorReportingAspect {
    public pointcut normalOperations();
    public pointcut criticalOperations() : call(* com..System.*(..));
}
