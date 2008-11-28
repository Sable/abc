class Test {
    // added following two lines to make it compile
    boolean cond1, cond2;
    void doSomething() { }

    int method() {
         /*[*/try {
             if(cond1) return 0;
             else if(cond2) return 1;
             System.out.println("Text");
         } finally {           
             doSomething();
         }/*]*/
         return 12;
    }
}