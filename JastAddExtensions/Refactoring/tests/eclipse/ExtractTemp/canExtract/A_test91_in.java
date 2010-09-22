package p;

import java.util.ArrayList;

class A {
    public void foo() {
        ArrayList<? super Integer> nl= new ArrayList<Integer>();
        Object o= nl.get(0);
    }
}
