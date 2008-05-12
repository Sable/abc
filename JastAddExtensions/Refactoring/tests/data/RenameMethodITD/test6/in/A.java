// RenameMethodITD/test6/in/A.java p HelloYield test() itdTest
package p;

import java.util.*;

class HelloYield {
   
    HelloYield h;
    public static void main(String[] args) {
        new HelloYield().run();
    }
   
    void run() {
        /*for(Integer i : ints()) {
            System.out.println(i);
           
        }*/
        test();
    }

   
    /**
      Hello
    */
    /*
    Iterable<Integer> ints() {
        yield return 1;
        yield return 2;
        for(int i = 3; i < 10; i++) {
            yield return i;
        }
        yield return 11;
    }
    */ 

}
aspect A {
    public void HelloYield.test() {
        System.out.println("Hello");
    }

}
