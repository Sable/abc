public aspect CallAndExecution {
    public static void main(String[] args) {
	C.run();
    }

    private static void p(String s) {
	System.out.println(s);
    }

    after() returning(): call(void I.x*())  { p("call I");  }
    after() returning(): call(void A.x*())  { p("call A");  }
    after() returning(): call(void B.x*())  { p("call B");  }

    after() returning(): execution(void I.x*())  { p("execution I");  }
    after() returning(): execution(void A.x*())  { p("execution A");  }
    after() returning(): execution(void B.x*())  { p("execution B");  }

    after() returning(): call(I.new())  { p("call new I");  }
    after() returning(): call(A.new())  { p("call new A");  }
    after() returning(): call(B.new())  { p("call new B");  }

    after() returning(): execution(I.new())  { p("execution new I");  }
    after() returning(): execution(A.new())  { p("execution new A");  }
    after() returning(): execution(B.new())  { p("execution new B");  }

    after() returning(): initialization(I.new())  { p("initialization I");  }
    after() returning(): initialization(A.new())  { p("initialization A");  }
    after() returning(): initialization(B.new())  { p("initialization B");  }

    after() returning(): preinitialization(I.new())  { p("preinitialization I");  }
    after() returning(): preinitialization(A.new())  { p("preinitialization A");  }
    after() returning(): preinitialization(B.new())  { p("preinitialization B");  }

    after(String s) returning(): set(String A.f*) && args(s)  { p("A: "+s); }
    after(String s) returning(): set(String B.f*) && args(s)  { p("B: "+s); }

    before(): initialization(M.new())  { p("initialization M");  }
    before(): initialization(J.new())  { p("initialization J");  }
    before(): initialization(K.new())  { p("initialization K");  }
    before(): initialization((I||J||K).new())  { p("initialization I|J|K");  }
}
