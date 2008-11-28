class A {
    public void test() {
        /*[*/int count=0;
        for(int j=0; j<100; j++) count++;/*]*/
        count=0;
        for(int j=0; j<100; j++) count++;
    }
}