package p;

import java.util.ArrayList;

class A {
    public void foo() {
        ArrayList<? super Integer> nl= new ArrayList<Integer>();
        Object temp= nl.get(0);
        Object o= temp;
    }
}
