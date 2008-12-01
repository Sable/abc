class Foo {
    {
	String[] args = getArgs();
	
	for(String arg : args) {
	    newMethod(arg);
	}
    }

    private void newMethod(String arg) {
        System.out.println("arg = " + arg);
    }

    // added following definition to make it compile
    String[] getArgs() { return null; }
}