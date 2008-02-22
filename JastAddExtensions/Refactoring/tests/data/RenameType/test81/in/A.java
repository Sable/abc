// RenameType/test81/in/A.java p B Entry
package p;

import java.util.Map.Entry;

class B {
}

public class A {
    public static void main(String[] args) {
        System.out.println(Entry.class.getName());
    }
}