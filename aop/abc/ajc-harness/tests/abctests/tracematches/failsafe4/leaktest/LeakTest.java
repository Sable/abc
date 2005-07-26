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
	    Vector v = new Vector(10);
            Enumeration e1 = new MyEnum(v);
            Enumeration e2 = new MyEnum(v);
            v.add(new Object());
	    Enumeration e3 = new MyEnum(v);
            e1.nextElement(); 
	    e2.nextElement();
    }
}
