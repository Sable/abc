class Foo {
    {
	String[] args = getArgs();
	
	for(String arg : args) {
	    /*[*/System.out.println("arg = " + arg);/*]*/
	}
    }

    // added following definition to make it compile
    String[] args() { return null; }
}