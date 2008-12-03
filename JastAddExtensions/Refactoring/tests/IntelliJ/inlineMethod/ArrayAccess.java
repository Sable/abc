class A {
    public void usage() {
	// changed next line to make it compile
        int array[] = new int[150];
        for (int i = 0; i < array.length; i++) {
            /*[*/method(array[i])/*]*/;
        }
    }
    public void method(int i) {
        System.out.println(i);
    }
}