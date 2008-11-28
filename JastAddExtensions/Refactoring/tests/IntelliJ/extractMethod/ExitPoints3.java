class Test{
    // added following line to make it compile
    boolean cond1, cond2, cond3;
    // added following method to make it compile
    void x() { }
    void foo() {
	if (cond1){      
	    /*[*/if (cond2) return;
	    x();/*]*/
	}
	else if (cond3){
	}
    }
}
