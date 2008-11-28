class Test{
    // added the following line to make it compile
    boolean cond1, cond2, cond3;
    public void foo() {
	if (cond1){
	    /*[*/if (cond2) return;/*]*/
	}
	else if (cond3){
	}
    }
}
