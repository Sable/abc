public aspect CflowBinding2 {
	
    pointcut flow(Object o): cflow(execution(void bar(int)) && this(o));

    before() : call(void m()) && flow(Object) {
    }

    before() : call(void m()) && flow(Object) {
    }
}

class Bar {
	void bar(int i) {
		m();
	}
	void m() {
	}
}

