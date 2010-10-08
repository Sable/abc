public aspect CorrectWithinSemantics {
	
	//TODO not yet a complete test case
	
	public void main(String args[]) {
		exhibit JP {};	
	}
	
	joinpoint void JP();
	
	before(): within(CorrectWithinSemantics) {
		System.err.println(thisJoinPointStaticPart);
	}
	
	before(): withincode(* main(..)) {
		System.err.println(thisJoinPointStaticPart);
	}
}