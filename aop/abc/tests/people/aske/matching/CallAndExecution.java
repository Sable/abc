public aspect CallAndExecution {
    public static void main(String[] args) {
	C.run();
    }

    private static void p(String s) {
	System.out.println(s);
    }

    after(): call(void I.x*())  { p("call I");  }
    after(): call(void A.x*())  { p("call A");  }
    after(): call(void B.x*())  { p("call B");  }

    after(): execution(void I.x*())  { p("execution I");  }
    after(): execution(void A.x*())  { p("execution A");  }
    after(): execution(void B.x*())  { p("execution B");  }

    after(): call(I.new())  { p("call new I");  }
    after(): call(A.new())  { p("call new A");  }
    after(): call(B.new())  { p("call new B");  }

    after(): execution(I.new())  { p("execution new I");  }
    after(): execution(A.new())  { p("execution new A");  }
    after(): execution(B.new())  { p("execution new B");  }

    after(): initialization(I.new())  { p("initialization I");  }
    after(): initialization(A.new())  { p("initialization A");  }
    after(): initialization(B.new())  { p("initialization B");  }

    after(): preinitialization(I.new())  { p("preinitialization I");  }
    after(): preinitialization(A.new())  { p("preinitialization A");  }
    after(): preinitialization(B.new())  { p("preinitialization B");  }

    after(String s): set(String A.f*) && args(s)  { p("A: "+s); }
    after(String s): set(String B.f*) && args(s)  { p("B: "+s); }
}
