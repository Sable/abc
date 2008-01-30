//Listing 8.20 PreserveBusinessException.java

public aspect PreserveBusinessException {
    declare precedence: PreserveBusinessException, BusinessConcernAspect;

    after() throwing(ConcernRuntimeException ex)
	throws BusinessException
	: call(* *.*(..) throws BusinessException) {
	Throwable cause = ex.getCause();
	if (cause instanceof BusinessException) {
	    throw (BusinessException)cause;
	}
	throw ex;
    }
}
