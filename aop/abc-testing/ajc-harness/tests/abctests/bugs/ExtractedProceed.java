public aspect ExtractedProceed {
    Object around() : execution(* *()) {
	RunnableWithReturn alg = new RunnableWithReturn(){
		public void run(){
			returnValue = proceed();				
		}
	};
	return null;
    }
}

abstract class RunnableWithReturn implements Runnable {
    protected Object returnValue;
	
    public Object getReturnValue(){
 	return returnValue;
    }
	
}
