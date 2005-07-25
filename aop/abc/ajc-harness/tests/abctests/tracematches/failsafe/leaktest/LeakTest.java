package leaktest;

import java.util.*;

class MyEnum implements Enumeration {

    int i;
    Vector v;

    MyEnum(Vector v) {
	this.v = v;
    }

    public boolean hasMoreElements() {
	return (i < v.size());
    }

    public Object nextElement() {
        return v.get(i);
    }
}

public class LeakTest {

    public static void main(String[] args) {
        Vector[] vs = new Vector[100];
	Enumeration[] es = new Enumeration[100];
	for(int i = 0; i<100; i++) {
	    vs[i] = new Vector(10);
            vs[i].add(new Object());
            es[i] = new MyEnum(vs[i]);
	}

        for(int i = 0; i<100; i++) {
            es[i].nextElement();
	}

        for(int i = 0; i<100; i++) {
            vs[i].add(null);
	}

        for(int i = 0; i<100; i++) {
            es[i].nextElement();
	}


    }
}