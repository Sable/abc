public aspect CflowBinding {
    Object around() : call(void m()) {
	    return null;
    }
}

class Bar {
    void bar() {
	m();
    }
    void m() {
    }
}

